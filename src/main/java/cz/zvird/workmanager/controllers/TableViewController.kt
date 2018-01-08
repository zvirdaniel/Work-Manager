package cz.zvird.workmanager.controllers

import cz.zvird.workmanager.data.DataHolder
import cz.zvird.workmanager.data.MemoryManager
import cz.zvird.workmanager.gui.LocalDateCell
import cz.zvird.workmanager.gui.errorNotification
import cz.zvird.workmanager.models.WorkSession
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.KeyCode
import javafx.util.Callback
import javafx.util.StringConverter
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

class TableViewController : Initializable {
	@FXML lateinit var table: TableView<WorkSession>
	@FXML lateinit var date: TableColumn<WorkSession, LocalDate>
	@FXML lateinit var time: TableColumn<WorkSession, LocalTime>
	@FXML lateinit var duration: TableColumn<WorkSession, Duration>
	@FXML lateinit var description: TableColumn<WorkSession, String>

	override fun initialize(location: URL?, resources: ResourceBundle?) {
		table.placeholder = Label("Žádná data k zobrazení. Lze přidat tlačítkem dole, nebo Ctrl + N.")
		keyHandlers()
		blankRowCallback()
		cellValueFactories()
		cellFactories()
		commitHandlers()
		DataHolder.addTableViewController(this)
	}

	/**
	 * Ctrl-N and Delete key handlers
	 */
	private fun keyHandlers() {
		table.onKeyPressed = EventHandler {
			if (it.isControlDown && it.code == KeyCode.N && !it.isShiftDown) {
				createNewRow()
			}

			if (it.code == KeyCode.DELETE) {
				if (table.selectionModel.selectedItem != null) {
					removeRow(table.selectionModel.selectedItem)
				}
			}
		}
	}

	/**
	 * Responsible for saving the data when cell factories detect a change
	 */
	private fun commitHandlers() {
		date.onEditCommit = EventHandler { it.rowValue.beginDateProperty.value = it.newValue }
		description.onEditCommit = EventHandler { it.rowValue.descriptionProperty.value = it.newValue }
		duration.onEditCommit = EventHandler { it.rowValue.durationProperty.value = it.newValue }
		time.onEditCommit = EventHandler { it.rowValue.beginTimeProperty.value = it.newValue }
	}

	/**
	 * Responsible for rendering the data contained within each cell for a single column
	 */
	private fun cellFactories() {
		date.cellFactory = Callback { LocalDateCell() }

		time.cellFactory = TextFieldTableCell.forTableColumn(object : StringConverter<LocalTime>() {
			override fun toString(time: LocalTime): String {
				return time.format(DateTimeFormatter.ofPattern("HH:mm"))
			}

			override fun fromString(string: String): LocalTime {
				try {
					if (string.contains(':')) {
						return LocalTime.parse(string, DateTimeFormatter.ofPattern("HH:mm"))
					}

					if (string.length == 4) {
						return LocalTime.parse(string, DateTimeFormatter.ofPattern("HHmm"))
					}

					return LocalTime.parse(string, DateTimeFormatter.ofPattern("HH"))
				} catch (e: DateTimeParseException) {
					errorNotification("$string není validní čas!")
				}

				return LocalTime.now(DataHolder.zone)
			}
		})

		duration.cellFactory = TextFieldTableCell.forTableColumn(object : StringConverter<Duration>() {
			override fun toString(duration: Duration): String {
				return duration.toString()
						.substring(2)
						.replace("(\\d[HMS])(?!$)".toRegex(), "$1 ")
						.toLowerCase()
			}

			override fun fromString(string: String): Duration {
				try {
					val minutes = string.toLong()
					return Duration.ofMinutes(minutes)
				} catch (e: NumberFormatException) {
					errorNotification("$string není validní počet minut! Pouze celé čísla.")
				}

				return Duration.ofMinutes(30)
			}
		})

		description.cellFactory = TextFieldTableCell.forTableColumn()
	}

	/**
	 * Responsible for populating the data for all cells within a single column
	 */
	private fun cellValueFactories() {
		date.cellValueFactory = Callback { it.value.beginDateProperty }
		time.cellValueFactory = Callback { it.value.beginTimeProperty }
		duration.cellValueFactory = Callback { it.value.durationProperty }
		description.cellValueFactory = Callback { it.value.descriptionProperty }
	}

	/**
	 * Double-click on blank space creates a new row
	 */
	private fun blankRowCallback() {
		table.rowFactory = Callback {
			val row = TableRow<WorkSession>()

			row.onMouseClicked = EventHandler {
				if (it.clickCount >= 2 && row.item !is WorkSession) {
					createNewRow()
				}
			}

			row
		}
	}

	/**
	 * Removes a row if table is not empty
	 */
	fun removeRow(row: WorkSession) {
		if (table.items.isNotEmpty()) {
			table.items.remove(row)
		}
	}

	/**
	 * Creates new row with some data, scrolls to the end of the table and selects it
	 */
	fun createNewRow() {
		val session: WorkSession
		val lastSession = table.items.lastOrNull()

		if (lastSession != null) {
			val lastDatePlusOneDay = lastSession.beginDateProperty.get().plusDays(1)
			session = if (lastDatePlusOneDay.monthValue == DataHolder.currentMonth) {
				val lastDateTime = LocalDateTime.of(lastDatePlusOneDay, LocalTime.of(12, 0))
				WorkSession(lastDateTime, 180, lastSession.descriptionProperty.value)
			} else {
				val lastDate = lastSession.beginDateProperty.get()
				val lastDateTime = LocalDateTime.of(lastDate, LocalTime.of(12, 0))
				WorkSession(lastDateTime, 180, lastSession.descriptionProperty.value)
			}
		} else {
			session = if (DataHolder.currentMonth == LocalDate.now(DataHolder.zone).monthValue) {
				WorkSession(addMinutes = 180, description = "Doplnit!")
			} else {
				val localDate = LocalDate.of(MemoryManager.currentYear, DataHolder.currentMonth, 1)
				val dateTime = LocalDateTime.of(localDate, LocalTime.of(12, 0))
				WorkSession(dateTime, 180, "Doplnit!")
			}
		}

		table.items.add(session)
		Platform.runLater {
			table.scrollTo(table.items.last())
			table.selectionModel.select(table.items.last())
		}
	}
}