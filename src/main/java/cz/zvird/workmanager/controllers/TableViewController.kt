package cz.zvird.workmanager.controllers

import cz.zvird.workmanager.data.DataHolder
import cz.zvird.workmanager.data.MemoryData
import cz.zvird.workmanager.gui.LocalDateCell
import cz.zvird.workmanager.models.WorkSession
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.KeyCode
import javafx.util.Callback
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

// TODO: Connect hourly wage to the controller
// TODO: Implement Time, Duration and Description cell value factories

class TableViewController : Initializable {
	@FXML lateinit var table: TableView<WorkSession>
	@FXML lateinit var date: TableColumn<WorkSession, LocalDate>
	@FXML lateinit var time: TableColumn<WorkSession, String>
	@FXML lateinit var duration: TableColumn<WorkSession, String>
	@FXML lateinit var description: TableColumn<WorkSession, String>
	@FXML lateinit var hourlyWageField: TextField

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
	}

	/**
	 * Responsible for rendering the data contained within each cell for a single column
	 */
	private fun cellFactories() {
		date.cellFactory = Callback { LocalDateCell() }
		time.cellFactory = TextFieldTableCell.forTableColumn()
		duration.cellFactory = TextFieldTableCell.forTableColumn()
		description.cellFactory = TextFieldTableCell.forTableColumn()
	}

	/**
	 * Responsible for populating the data for all cells within a single column
	 */
	private fun cellValueFactories() {
		date.cellValueFactory = Callback { it.value.beginDateProperty }
		time.cellValueFactory = Callback { it.value.beginTimeProperty.asString() }
		duration.cellValueFactory = Callback { it.value.durationProperty.asString() }
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
		// TODO: fix hourly wage
		val session: WorkSession
		val currentMonth = DataHolder.currentTab + 1
		val lastSession = table.items.lastOrNull()

		if (lastSession != null) {
			val lastDatePlusOneDay = lastSession.beginDateProperty.get().plusDays(1)
			if (lastDatePlusOneDay.monthValue == currentMonth) {
				val lastDateTime = LocalDateTime.of(lastDatePlusOneDay, LocalTime.of(12, 0))
				session = WorkSession(lastDateTime, 180, 0, lastSession.descriptionProperty.value)
			} else {
				val lastDate = lastSession.beginDateProperty.get()
				val lastDateTime = LocalDateTime.of(lastDate, LocalTime.of(12, 0))
				session = WorkSession(lastDateTime, 180, 0, lastSession.descriptionProperty.value)
			}
		} else {
			if (currentMonth == LocalDate.now().monthValue) {
				session = WorkSession(addMinutes = 180, hourlyWage = 0, description = "Doplnit!")
			} else {
				val localDate = LocalDate.of(MemoryData.currentYear, currentMonth, 1)
				val dateTime = LocalDateTime.of(localDate, LocalTime.of(12, 0))
				session = WorkSession(dateTime, 180, 0, "Doplnit!")
			}
		}

		table.items.add(session)
		Platform.runLater {
			table.scrollTo(table.items.last())
			table.selectionModel.select(table.items.last())
		}
	}
}