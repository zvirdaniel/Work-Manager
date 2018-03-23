package cz.zvird.workmanager.gui

import cz.zvird.workmanager.data.DataHolder
import cz.zvird.workmanager.data.MemoryManager
import cz.zvird.workmanager.models.WorkSession
import javafx.application.Platform
import javafx.scene.control.ContentDisplay
import javafx.scene.control.DateCell
import javafx.scene.control.DatePicker
import javafx.scene.control.TableCell
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.util.Callback
import javafx.util.StringConverter
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

/**
 * Custom cell with a date picker
 */
class LocalDateCell : TableCell<WorkSession, LocalDate>() {
	private val datePicker = DatePicker()
	private var oldValue: LocalDate? = null

	init {
		initDatePicker()

		/**
		 * Starts editing when double clicking the cell
		 */
		setOnMouseClicked {
			if (it.clickCount == 2) {
				startEdit()
			}
		}

		/**
		 * Informs the DataHolder about canceling the edit
		 */
		this.addEventFilter(KeyEvent.KEY_PRESSED) {
			if (!it.isShiftDown && !it.isControlDown && !it.isAltDown && it.code == KeyCode.ESCAPE) {
				DataHolder.editCellCancelNow = true
			}
		}
	}

	/**
	 * Makes date picker visible, requests focus, waits 100ms in another thread, and selects all text in a TextField
	 */
	override fun startEdit() {
		super.startEdit()
		contentDisplay = ContentDisplay.GRAPHIC_ONLY

		oldValue = datePicker.value

		thread {
			Thread.sleep(150)
			Platform.runLater {
				datePicker.requestFocus()
				datePicker.editor.selectAll()
			}
		}
	}

	/**
	 * Hides date picker and commits the value to the controller
	 */
	override fun commitEdit(newValue: LocalDate?) {
		super.commitEdit(newValue)
		datePicker.value = newValue
		contentDisplay = ContentDisplay.TEXT_ONLY

		/**
		 * There is a bug in the JavaFX platform, which does not commit the value properly, if the new value is the same as the old value
		 * The following code manages focus loss, and tells the row editor to proceed editing the next cell
		 */
		if (oldValue == newValue) {
			if (DataHolder.getTableViewController().rowEditorActive) {
				DataHolder.editCellFinishNow = true
				return
			}

			val row = this.tableRow.index
			val column = this.tableColumn
			if (column != null) {
				DataHolder.getTableViewController().focusCell(row, column)
			}
		}
	}

	/**
	 * Hides date picker, does not commit any values
	 */
	override fun cancelEdit() {
		super.cancelEdit()
		contentDisplay = ContentDisplay.TEXT_ONLY
	}

	/**
	 * Updates TableCell with dates
	 */
	override fun updateItem(item: LocalDate?, empty: Boolean) {
		super.updateItem(item, empty)

		if (item == null || empty) {
			reset()
		} else {
			datePicker.value = item
			text = dateToFancyString(item)
			graphic = datePicker
			contentDisplay = ContentDisplay.TEXT_ONLY
		}
	}

	/**
	 * Resets text and graphic to null
	 * Gets called when a cell is deleted from a table
	 */
	private fun reset() {
		text = null
		graphic = null
		contentDisplay = ContentDisplay.TEXT_ONLY
	}

	/**
	 * Initializes date picker attributes and behaviour
	 */
	private fun initDatePicker() {
		datePicker.isEditable = true
		datePicker.promptText = "dd. mm. yyyy"
		datePicker.converter = generateLocalDateConverter()

		/**
		 * Filters the available dates in the graphical date picker to the given month
		 */
		val dayCellFactory = Callback<DatePicker, DateCell> {
			object : DateCell() {
				override fun updateItem(item: LocalDate, empty: Boolean) {
					super.updateItem(item, empty)

					val currentMonth = Month.of(DataHolder.currentMonth)
					val firstDayOfCurrentMonth = LocalDate.of(MemoryManager.currentYear, currentMonth, 1)
					val firstDayOfPreviousMonth = firstDayOfCurrentMonth.minusMonths(1)
					val lastDayOfPreviousMonth = firstDayOfPreviousMonth.withDayOfMonth(firstDayOfPreviousMonth.lengthOfMonth())
					val firstDayOfNextMonth = firstDayOfCurrentMonth.plusMonths(1)

					if (item.isBefore(lastDayOfPreviousMonth) || item.isEqual(lastDayOfPreviousMonth) ||
							item.isAfter(firstDayOfNextMonth) || item.isEqual(firstDayOfNextMonth)) {
						isDisable = true
						style = "-fx-background-color: #ffc0cb;"
					}
				}
			}
		}

		datePicker.dayCellFactory = dayCellFactory

		var lastDate = datePicker.value

		/**
		 * Sets last used date to a variable when opening the date picker
		 */
		datePicker.setOnShown {
			lastDate = datePicker.value
		}

		/**
		 * Commits the change and closes the date picker if the user picks a new date
		 */
		datePicker.setOnHidden {
			val newValue = datePicker.value
			if (newValue != null && newValue != lastDate) {
				Platform.runLater {
					val date = datePicker.value
					commitEdit(date)
				}
			}
		}

		/**
		 * There is a bug in the JavaFX platform, which does not commit value of its TextField when pressing the ENTER key
		 * This fixes the problem, it overrides the default behaviour when pressing the ENTER key in a TextField
		 */
		datePicker.addEventFilter(KeyEvent.KEY_PRESSED, {
			if (it.code == KeyCode.ENTER) {
				val text = datePicker.editor.text

				if (text != null) {
					it.consume()
					try {
						val date = datePicker.converter.fromString(text)
						commitEdit(date)
					} catch (e: Exception) {
						val error = "$text není validní den. Pouze dny v měsíci!"
						errorNotification(error)
					}
				}
			}
		})
	}

	/**
	 * Converts LocalDate to string with full date
	 */
	private fun dateToFancyString(date: LocalDate?): String? {
		val formatter = DateTimeFormatter.ofPattern("dd. MM. yyyy")

		if (date != null) {
			return formatter.format(date)
		}

		return null
	}

	/**
	 * StringConverter converts LocalDate to string and vice versa
	 */
	private fun generateLocalDateConverter(): StringConverter<LocalDate> {
		return object : StringConverter<LocalDate>() {
			/**
			 * Converts LocalDate to short string
			 */
			override fun toString(date: LocalDate?): String? {
				val formatter = DateTimeFormatter.ofPattern("dd.")

				if (date != null) {
					return formatter.format(date)
				}

				return null
			}

			/**
			 * Parses text into LocalDate
			 */
			override fun fromString(text: String?): LocalDate? {
				val textWithoutDots = text?.replace('.', ' ')
				val textWithoutSpaces = textWithoutDots?.replace("[\\s|\u00A0]+".toRegex(), "")

				if (textWithoutSpaces != null && textWithoutSpaces.isNotEmpty()) {
					val day = textWithoutSpaces.toInt()
					return LocalDate.of(MemoryManager.currentYear, DataHolder.currentMonth, day)
				}

				return null
			}
		}
	}
}