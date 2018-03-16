package cz.zvird.workmanager.controllers

import cz.zvird.workmanager.data.DataHolder
import cz.zvird.workmanager.data.FileManager
import cz.zvird.workmanager.data.MemoryManager
import cz.zvird.workmanager.gui.*
import cz.zvird.workmanager.models.WorkSession
import cz.zvird.workmanager.models.writeYearInXlsx
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.scene.text.Text
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Window
import org.jetbrains.kotlin.utils.addToStdlib.sumByLong
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread

/**
 * Controller for the main UI, not controlling TableView
 */
class MainController : Initializable {
	@FXML lateinit var tabPane: TabPane
	@FXML lateinit var openFileMenu: MenuItem
	@FXML lateinit var saveFileMenu: MenuItem
	@FXML lateinit var saveAsFileMenu: MenuItem
	@FXML lateinit var newFileMenu: MenuItem
	@FXML lateinit var aboutMenu: MenuItem
	@FXML lateinit var clearMonthMenu: MenuItem
	@FXML lateinit var exportMenu: MenuItem
	@FXML lateinit var deleteButton: Button
	@FXML lateinit var newRowButton: Button
	@FXML lateinit var stackPane: StackPane
	@FXML lateinit var hourlyWageField: TextField
	@FXML lateinit var wageText: Text
	@FXML lateinit var progress: ProgressBar
	private lateinit var window: Window

	private val listChangeListener = ListChangeListener<WorkSession> {
		refreshBottomBarUI()
	}

	override fun initialize(location: URL?, resources: ResourceBundle?) {
		// Adds the controller to the DataHolder in order to be accessible everywhere
		DataHolder.mainController = this

		newFileMenu.onAction = EventHandler { newFileUI() }
		openFileMenu.onAction = EventHandler { openFileUI() }
		saveFileMenu.onAction = EventHandler { saveFileUI() }
		saveAsFileMenu.onAction = EventHandler { saveFileAsUI() }
		deleteButton.onAction = EventHandler { deleteRow() }
		newRowButton.onAction = EventHandler { newRow() }
		aboutMenu.onAction = EventHandler { showAboutDialog(window) }
		exportMenu.onAction = EventHandler { exportFileUI() }
		clearMonthMenu.onAction = EventHandler { clearCurrentMonth() }
		hourlyWageField.onKeyPressed = EventHandler { hourlyWageKeyPress(it) }

		// MaskerPane is used to block the UI if needed
		stackPane.children.add(DataHolder.maskerPane)

		// Requests focus, scrolls to the end of the table, sets DataHolder.currentTab, and hooks listeners for wage calculation
		tabPane.selectionModel.selectedIndexProperty().addListener { _, oldValue, newValue ->
			DataHolder.currentTab = newValue.toInt()
			val oldTab = DataHolder.getTableViewController(oldValue.toInt())
			val newTab = DataHolder.getTableViewController()
			Platform.runLater {
				newTab.table.requestFocus()
				if (newTab.table.items.isNotEmpty()) {
					newTab.table.scrollTo(newTab.table.items.count() - 1)
				}

				refreshBottomBarUI()

				oldTab.table.items.removeListener(listChangeListener)
				newTab.table.items.addListener(listChangeListener)
				newTab.table.selectionModel.selectLast()
			}
		}

		// Select tab with current month
		val currentMonthIndex = Date().toInstant().atZone(DataHolder.zone).toLocalDate().month.value - 1
		tabPane.selectionModel.select(currentMonthIndex)
		DataHolder.currentTab = currentMonthIndex

		// Assigns the window variable after it is loaded properly, adds a listener to the current tab
		Platform.runLater {
			window = tabPane.scene.window
			DataHolder.getTableViewController().table.items.addListener(listChangeListener)
		}

		// Hide ProgressBar when app launches
		progress.visibleProperty().value = false
	}

	/**
	 * Deletes data of the currently selected month
	 */
	private fun clearCurrentMonth() {
		MemoryManager.getMonth().sessions.clear()
	}

	/**
	 * Changes the hourly wage for the current tab, and updates the bottom bar UI
	 */
	private fun hourlyWageKeyPress(event: KeyEvent) {
		if (event.code == KeyCode.ENTER) {
			if (hourlyWageField.text.trim().isEmpty()) {
				errorNotification("Nebylo zadáno číslo!")
				return
			}

			try {
				val hourlyWage = hourlyWageField.text.toInt()
				if (hourlyWage <= 0) throw IllegalArgumentException("Hourly wage must be bigger than zero!")

				MemoryManager.getMonth().hourlyWage = hourlyWage

				informativeNotification("Hodinový plat byl změněn na $hourlyWage Kč.")
				refreshBottomBarUI()
			} catch (e: Exception) {
				errorNotification("${hourlyWageField.text} není celé číslo větší než 0!")
			}
		}
	}

	/**
	 * Recalculates total time and monthly wage for the current month, and displays it in the bottom bar
	 */
	fun refreshBottomBarUI() {
		val sessions = DataHolder.getTableViewController().table.items
		val hourlyWage = MemoryManager.getMonth().hourlyWage
		hourlyWageField.text = hourlyWage.toString()
		val duration = Duration.ofMinutes(sessions.sumByLong { it.durationProperty.value.toMinutes() })
		val monthlyWageGross = (hourlyWage * duration.toHours()).toInt()
		var monthlyWage = monthlyWageGross
		var tax = 0

		if (monthlyWageGross > 10000) {
			val superGrossWage = monthlyWageGross.toDouble() * 1.34
			val superGrossWageRounded = BigDecimal(superGrossWage / 100.0).setScale(0, RoundingMode.HALF_UP)
			val incomeTax = superGrossWageRounded.toDouble() * 100.0 * 0.15
			monthlyWage = (monthlyWageGross * 0.89 - incomeTax + 2070).toInt()
			tax = monthlyWageGross - monthlyWage
		}

		val monthlyWageText = when (tax) {
			0 -> "Mzda: ${formatWage(monthlyWage)} Kč"
			else -> "Hrubá mzda: ${formatWage(monthlyWageGross)} Kč -> Čistá mzda: ${formatWage(monthlyWage)} Kč -> Daň: ${formatWage(tax)} Kč"
		}

		val hoursText = "Čas celkem: " +
				duration.toString()
						.substring(2)
						.replace("(\\d[HMS])(?!$)".toRegex(), "$1 ")
						.toLowerCase()

		wageText.text = "$hoursText -> $monthlyWageText"
	}

	/**
	 * @param wage number to format
	 * @return string containing a number separated every 3 digits with spaces
	 */
	private fun formatWage(wage: Int): String {
		val charArray = wage.toString().toCharArray()
		val length = charArray.size
		var groups = length / 3
		var remainder = length % 3

		var result = ""
		var index = 0

		while (remainder > 0) {
			result += charArray[index]
			index++
			remainder--
		}

		while (groups > 0) {
			result += ' '
			groups--

			for (i in 0 until 3) {
				result += charArray[index]
				index++
			}
		}

		return result
	}

	/**
	 * Opens an export dialog (file selector, and month range selector) and exports the file
	 */
	private fun exportFileUI() {
		val pair = showExportFileDialog(window) ?: return // Terminates exporting if dialog was closed with the cancel button
		val monthRange = pair.first
		val file = pair.second

		if (file != null) {
			val blockedTask = object : BlockedTask({
				writeYearInXlsx(file, monthRange)
			}) {
				override fun succeeded() {
					savedAsNotification(file.name)
				}

				override fun failed() {
					this.exception.printStackTrace()
					cantSaveNotification(file.name)
				}
			}

			thread { blockedTask.run() }
		}
	}

	/**
	 * Creates a new row in currently opened tab by calling its controller
	 */
	private fun newRow() {
		val currentTab = DataHolder.getTableViewController()
		currentTab.createNewRow()
	}

	/**
	 * Deletes a row by calling the controller for currently opened tab
	 */
	private fun deleteRow() {
		val currentTab = DataHolder.getTableViewController()
		val currentRow = currentTab.table.selectionModel.selectedItem
		if (currentRow != null) {
			currentTab.removeRow(currentRow)
		}
	}

	/**
	 * Opens a file selector and creates new file
	 */
	private fun newFileUI() {
		val year = showYearSelectorDialog(window)

		if (year == null || year <= 0) {
			errorNotification("Špatně zadaný rok!")
			return
		}

		val file = showSaveFileDialog("Vytvořit nový soubor",
				filters = listOf(ExtensionFilter("JSON", "*.json")),
				extension = ".json",
				initialFileName = year.toString(),
				ownerWindow = window)

		if (file != null) {
			try {
				FileManager.new(file, year)
				FileManager.load(file)
				savedAsNotification(file.name)
				MemoryManager.isChanged = false
			} catch (e: Exception) {
				cantSaveNotification(file.name)
			}
		}
	}

	/**
	 * Saves the file and notifies the user
	 */
	private fun saveFileUI() {
		try {
			FileManager.save()
			savedAsNotification(FileManager.retrieve().name)
			MemoryManager.isChanged = false
		} catch (e: Exception) {
			cantSaveNotification(FileManager.retrieve().name)
		}
	}

	/**
	 * Opens a file selector, and saves the file
	 */
	private fun saveFileAsUI() {
		val originalFile = FileManager.retrieve()

		val file = showSaveFileDialog(title = "Uložit soubor jako",
				filters = listOf(ExtensionFilter("JSON", "*.json")),
				initialDir = File(originalFile.parent),
				initialFileName = originalFile.nameWithoutExtension + " kopie",
				extension = ".json",
				ownerWindow = window
		)


		if (file != null) {
			try {
				FileManager.save(file)
				savedAsNotification(file.name)
				MemoryManager.isChanged = false
			} catch (e: Exception) {
				cantSaveNotification(file.name)
			}
		}
	}

	/**
	 * Opens a file selector in home directory and opens the file
	 */
	private fun openFileUI() {
		val file = showOpenFileDialog(
				title = "Otevřít soubor",
				filters = listOf(ExtensionFilter("JSON", "*.json")),
				ownerWindow = window
		)

		if (file != null) {
			Platform.runLater {
				BlockedTask {
					try {
						FileManager.load(file, true)
						MemoryManager.isChanged = false
					} catch (e: Exception) {
						informativeNotification("Soubor nelze otevřít, nebo není validní.")
					}
				}.run()
			}
		}
	}
}