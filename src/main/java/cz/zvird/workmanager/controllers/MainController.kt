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
 * Controller for the main user interface, not controlling TableView
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

	internal val listChangeListener = ListChangeListener<WorkSession> {
		if (DataHolder.dataListenerEnabled) {
			refreshBottomBarUI()
		}
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
		hourlyWageField.onKeyPressed = EventHandler { hourlyWageEnterKeyPress(it) }

		stackPane.addEventFilter(KeyEvent.KEY_PRESSED) {
			if (it.code == KeyCode.F2) {
				saveFileUI()
			}
		}

		// MaskerPane is used to block the user interface if needed
		stackPane.children.add(DataHolder.maskerPane)

		// On every tab selection: sorts the table, hooks up the change listener (used to calculate wage), refreshes the bottom bar
		tabPane.selectionModel.selectedIndexProperty().addListener { _, oldValue, newValue ->
			DataHolder.editCellCancelNow = true // Terminates any row editor instances
			DataHolder.currentTab = newValue.toInt()
			val oldTab = DataHolder.getTableViewController(oldValue.toInt())
			val newTab = DataHolder.getTableViewController()
			val newTable = newTab.table

			Platform.runLater {
				sortTableAndFocus(newTable)
				refreshBottomBarUI()
			}

			oldTab.table.items.removeListener(listChangeListener)
			newTable.items.addListener(listChangeListener)
		}

		// Selects tab with the current month
		val currentMonthIndex = Date().toInstant().atZone(DataHolder.zone).toLocalDate().month.value - 1
		tabPane.selectionModel.select(currentMonthIndex)
		DataHolder.currentTab = currentMonthIndex

		// Assigns the window variable after it is loaded properly, adds a listener to the current tab
		Platform.runLater {
			window = tabPane.scene.window
			DataHolder.getTableViewController().table.items.addListener(listChangeListener)
		}

		// Hides the ProgressBar on application initialization
		progress.visibleProperty().value = false

		// Waits until the app launches and is fully visible (i.e. with data loaded) and sorts the table
		thread {
			val tableView = DataHolder.getTableViewController().table
			while (!tableView.isFocused || !tableView.isVisible) {
				Thread.sleep(100)
			}

			Thread.sleep(100)
			Platform.runLater {
				sortTableAndFocus()
			}
		}
	}

	/**
	 * Removes previous sorting properties of the given table view, and sorts all data in ascending order by its first column
	 * The, requests focus to the last row in the given table
	 * @param tableView to sort, implicitly the currently opened table view
	 */
	fun sortTableAndFocus(tableView: TableView<WorkSession> = DataHolder.getTableViewController().table) {
		val sortColumn = tableView.columns.first()
		sortColumn.sortType = TableColumn.SortType.ASCENDING
		tableView.sortOrder.clear()
		tableView.sortOrder.add(sortColumn)
		tableView.sort()

		tableView.requestFocus()
		if (tableView.items.isNotEmpty()) {
			tableView.scrollTo(tableView.items.count() - 1)
		}

		tableView.selectionModel.selectLast()
	}

	/**
	 * Deletes data of the currently selected month
	 */
	private fun clearCurrentMonth() {
		MemoryManager.getMonth().sessions.clear()
	}

	/**
	 * Changes the hourly wage for the current tab, and updates the bottom bar user interface
	 */
	private fun hourlyWageEnterKeyPress(event: KeyEvent) {
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
	private fun refreshBottomBarUI() {
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
	 * Opens an export dialog (file selector, and a month range selector) and exports the file
	 */
	private fun exportFileUI() {
		val pair = showExportFileDialog(window) ?: return // Terminates exporting if dialog was closed with the cancel button
		val monthRange = pair.first
		val file = pair.second

		if (file != null) {
			BlockedTask {
				try {
					writeYearInXlsx(file, monthRange)
					savedAsNotification(file.name)
				} catch (e: Exception) {
					e.printStackTrace()
					cantSaveNotification(file.name)
				}
			}
		}
	}

	/**
	 * Creates a new row in the currently opened tab
	 */
	private fun newRow() {
		val currentTab = DataHolder.getTableViewController()
		currentTab.createNewRow()
	}

	/**
	 * Deletes a row in the currently opened tab
	 */
	private fun deleteRow() {
		val currentTab = DataHolder.getTableViewController()
		val currentRow = currentTab.table.selectionModel.selectedItem
		if (currentRow != null) {
			currentTab.removeRow(currentRow)
		}
	}

	/**
	 * Opens a file selector and creates a new file
	 */
	private fun newFileUI() {
		val string = showYearSelectorDialog(window)

		if (string == null) {
			informativeNotification("Vytváření souboru přerušeno uživatelem.")
			return
		}

		val year = string.toIntOrNull()
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
			BlockedTask {
				try {
					FileManager.new(file, year)
					FileManager.load(file)
					savedAsNotification(file.name)
				} catch (e: Exception) {
					cantSaveNotification(file.name)
				}
			}
		}
	}

	/**
	 * Saves the file and notifies the user
	 */
	private fun saveFileUI() {
		BlockedTask {
			try {
				FileManager.save()
				savedAsNotification(FileManager.retrieve().name)
			} catch (e: Exception) {
				cantSaveNotification(FileManager.retrieve().name)
			}
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
			BlockedTask {
				try {
					FileManager.save(file)
					savedAsNotification(file.name)
				} catch (e: Exception) {
					cantSaveNotification(file.name)
				}
			}
		}
	}

	/**
	 * Opens a file selector in the home directory and opens the selected file
	 */
	private fun openFileUI() {
		val file = showOpenFileDialog(
				title = "Otevřít soubor",
				filters = listOf(ExtensionFilter("JSON", "*.json")),
				ownerWindow = window
		)

		if (file != null) {
			BlockedTask {
				try {
					FileManager.load(file)
				} catch (e: Exception) {
					informativeNotification("Soubor nelze otevřít, nebo není validní.")
				}
			}
		}
	}
}