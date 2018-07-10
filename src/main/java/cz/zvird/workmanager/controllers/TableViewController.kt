package cz.zvird.workmanager.controllers

import cz.zvird.workmanager.controllers.TableViewController.EditingStateHolder.EditingState.*
import cz.zvird.workmanager.data.DataHolder
import cz.zvird.workmanager.data.MemoryManager
import cz.zvird.workmanager.gui.LocalDateCell
import cz.zvird.workmanager.gui.generateDurationTextCell
import cz.zvird.workmanager.gui.generateLocalTimeTextCell
import cz.zvird.workmanager.gui.generateStringTextCell
import cz.zvird.workmanager.models.WorkSession
import cz.zvird.workmanager.safeCall
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyEvent
import javafx.util.Callback
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.concurrent.thread

/**
 * Controller for the table view, each table has its own instance
 */
class TableViewController : Initializable {
	@FXML lateinit var table: TableView<WorkSession>
	@FXML lateinit var date: TableColumn<WorkSession, LocalDate>
	@FXML lateinit var time: TableColumn<WorkSession, LocalTime>
	@FXML lateinit var duration: TableColumn<WorkSession, Duration>
	@FXML lateinit var description: TableColumn<WorkSession, String>

	override fun initialize(location: URL?, resources: ResourceBundle?) {
		table.placeholder = Label("Žádná data k zobrazení. Lze přidat tlačítkem dole, nebo Ctrl + N.")
		addKeyboardHandlers()
		blankRowCallback()
		cellValueFactories()
		cellFactories()
		commitHandlers()
		DataHolder.addTableViewController(this)
	}

	/**
	 * Adds table view key handlers
	 */
	private fun addKeyboardHandlers() {
		table.addEventFilter(KeyEvent.KEY_PRESSED) {
			when {
				it.isControlDown && !it.isShiftDown && it.code == DELETE && table.selectionModel.selectedItem != null ->
					removeRow(table.selectionModel.selectedItem)

				it.isControlDown && !it.isShiftDown && it.code == N ->
					addRow()

				it.code == INSERT ->
					addRow()

				it.code == F5 ->
					editCurrentRow()

				it.isControlDown && !it.isShiftDown && it.code == E ->
					editCurrentRow()
			}
		}
	}

	/**
	 * Responsible for saving the data when cell factories commit a change
	 */
	private fun commitHandlers() {
		date.onEditCommit = EventHandler { it.rowValue.beginDateProperty.value = it.newValue }
		description.onEditCommit = EventHandler { it.rowValue.descriptionProperty.value = it.newValue }
		duration.onEditCommit = EventHandler { it.rowValue.durationProperty.value = it.newValue }
		time.onEditCommit = EventHandler { it.rowValue.beginTimeProperty.value = it.newValue }
	}

	/**
	 * Responsible for rendering the data and managing the user interface of each cell
	 */
	private fun cellFactories() {
		date.cellFactory = Callback { LocalDateCell() }
		time.cellFactory = Callback { generateLocalTimeTextCell() }
		duration.cellFactory = Callback { generateDurationTextCell() }
		description.cellFactory = Callback { generateStringTextCell() }
	}

	/**
	 * Responsible for populating the cells with data, which are rendered by the cell factories
	 */
	private fun cellValueFactories() {
		date.cellValueFactory = Callback { it.value.beginDateProperty }
		time.cellValueFactory = Callback { it.value.beginTimeProperty }
		duration.cellValueFactory = Callback { it.value.durationProperty }
		description.cellValueFactory = Callback { it.value.descriptionProperty }
	}

	/**
	 * Adds a call back to the table, which makes double-clicking the blank space create a new row
	 */
	private fun blankRowCallback() {
		table.rowFactory = Callback {
			val row = TableRow<WorkSession>()

			row.onMouseClicked = EventHandler {
				if (it.clickCount >= 2 && row.item !is WorkSession) {
					addRow()
				}
			}

			row
		}
	}

	/**
	 * Removes a row if the table is not empty
	 */
	fun removeRow(row: WorkSession) {
		if (table.items.isNotEmpty()) {
			table.items.remove(row)
		}
	}

	/**
	 * Creates new row with data, scrolls to the end of the table and selects it
	 * @param session will be generated if null
	 */
	fun addRow(session: WorkSession? = null) {
		val newSession: WorkSession
		val lastSession = table.items.lastOrNull()

		if (session != null) {
			newSession = session
		} else {
			newSession = if (lastSession != null) {
				val lastDatePlusOneDay = lastSession.beginDateProperty.get().plusDays(1)
				if (lastDatePlusOneDay.monthValue == DataHolder.currentMonth) {
					val lastDateTime = LocalDateTime.of(lastDatePlusOneDay, LocalTime.of(12, 0))
					WorkSession(lastDateTime, 180, lastSession.descriptionProperty.value)
				} else {
					val lastDate = lastSession.beginDateProperty.get()
					val lastDateTime = LocalDateTime.of(lastDate, LocalTime.of(12, 0))
					WorkSession(lastDateTime, 180, lastSession.descriptionProperty.value)
				}
			} else {
				val localDate = LocalDate.of(MemoryManager.currentYear, DataHolder.currentMonth, 1)
				val dateTime = LocalDateTime.of(localDate, LocalTime.of(12, 0))
				WorkSession(dateTime, 180, "Doplnit!")
			}
		}

		table.items.add(newSession)
		Platform.runLater {
			table.scrollTo(table.items.last())
			table.requestFocus()
			table.selectionModel.selectLast()
		}
	}

	/**
	 * Listener informs the row editor about changes in the current column by editing the state
	 * @param state to modify when the user makes a change to the column
	 * @return ChangeListener that can be added or removed from any given observable value
	 */
	private fun <T> generateChangeListener(state: EditingStateHolder): ChangeListener<T> = ChangeListener { _, _, _ ->
		state.value = FINISHED
	}

	/**
	 * State can not be stored simply in the enum variable, because just it's value is being passed to the function, not the variable reference,
	 * therefore those functions can not edit the state. Example: fun (s: State) { s = INIT } will not work, because the reference to the
	 * variable "s" is not passed, only it's value is. So the function actually attempts something like "INIT = INIT" which does not work.
	 * The solution is to hold the variable in a class, and pass the class instance to the functions instead.
	 */
	private class EditingStateHolder(initial: EditingState) {
		enum class EditingState { EDITING, CANCELED, FINISHED }

		var value = initial
	}

	private var editingRow = 0 // Used in the row editor to determine the currently used row
	var rowEditorActive = false // Used in the row editor, controls the thread state, ensures only one row editor at a time is possible

	/**
	 * Edits the currently selected row (cell after cell), one row at a time
	 */
	private fun editCurrentRow() {
		if (rowEditorActive) {
			rowEditorActive = false // Terminates all threads depending on this variable
		}

		editingRow = table.selectionModel.selectedIndex

		if (table.items.isEmpty() || table.items[editingRow] == null) {
			return
		}

		rowEditorActive = true
		thread {
			while (rowEditorActive) {
				try {
					editCell(date)
					editCell(time)
					editCell(duration)
					editCell(description)

					Thread.sleep(100)
					Platform.runLater {
						table.requestFocus()
						table.selectionModel.select(editingRow)
						table.focusModel.focus(editingRow, description)
					}
				} catch (e: IllegalStateException) {
					println("DEBUG: ${e.message}")
				}

				rowEditorActive = false
			}
		}
	}

	/**
	 * Edits the given column in the current row, cancels the edit on the focus loss
	 * @param column to edit on the given row
	 * @throws IllegalStateException if editing was canceled
	 */
	private fun <T> editCell(column: TableColumn<WorkSession, T>) {
		// Used in the exception
		var message = "Editing in column '${column.text}' on row $editingRow has been canceled "

		// ProgressBar initialization
		val progressBar = DataHolder.mainController.progress
		progressBar.visibleProperty().value = true

		// State management
		DataHolder.editCellFinishNow = false
		DataHolder.editCellCancelNow = false
		val editingState = EditingStateHolder(EDITING)
		val listener = generateChangeListener<T>(editingState)
		column.getCellObservableValue(editingRow).addListener(listener)

		// User interface management
		Platform.runLater {
			table.selectionModel.select(editingRow, column)
			table.focusModel.focus(editingRow, column)
			table.edit(editingRow, column)
			progressBar.progressProperty().value = -1.0
		}

		// State management
		while (editingState.value == EDITING) {
			Thread.sleep(75)

			val focusedCell = table.focusModel.focusedCell
			val focusedRow = focusedCell.row

			if (DataHolder.editCellCancelNow) {
				message += "via the global variable"
				editingState.value = CANCELED
			}

//			if (focusedCell.tableColumn != column) {
//				message += "because of focus loss on the column"
//				editingState.value = CANCELED
//			}

			if (focusedRow != editingRow) {
				message += "because of focus loss on the row"
				editingState.value = CANCELED
			}

			if (DataHolder.editCellFinishNow) {
				editingState.value = FINISHED
			}
		}

		// Termination
		column.getCellObservableValue(editingRow).removeListener(listener)
		progressBar.visibleProperty().value = false
		if (editingState.value == CANCELED) {
			throw IllegalStateException(message)
		}
	}

	/**
	 * Sets focus to the given column on the given row
	 */
	fun <T> focusCell(row: Int, column: TableColumn<WorkSession, T>) {
		safeCall {
			table.requestFocus()
			table.selectionModel.select(row, column)
			table.focusModel.focus(row, column)
		}
	}
}