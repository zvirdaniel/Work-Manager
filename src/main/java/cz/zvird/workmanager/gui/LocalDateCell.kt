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
 * Custom cell with a DatePicker
 */
class LocalDateCell : TableCell<WorkSession, LocalDate>() {
	private val datePicker = DatePicker()

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
	}

	/**
	 * Makes DatePicker visible, requests focus, waits 100ms in another thread, and selects all text in a TextField
	 */
	override fun startEdit() {
		super.startEdit()
		contentDisplay = ContentDisplay.GRAPHIC_ONLY
		datePicker.requestFocus()

		thread(start = true, isDaemon = false, block = {
			Thread.sleep(100)
			Platform.runLater { datePicker.editor.selectAll() }
		})
	}

	/**
	 * Hides DatePicker and commits the value to the controller
	 */
	override fun commitEdit(newValue: LocalDate?) {
		super.commitEdit(newValue)
		datePicker.value = newValue
		contentDisplay = ContentDisplay.TEXT_ONLY
	}

	/**
	 * Hides DatePicker, does not commit any values
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
			contentDisplay = ContentDisplay.TEXT_ONLY // TEXT_ONLY should be set when not editing
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
	 * Initialises DatePicker attributes and behaviour
	 */
	private fun initDatePicker() {
		datePicker.isEditable = true
		datePicker.promptText = "dd. mm. yyyy"
		datePicker.converter = generateLocalDateConverter()

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
		 * Sets last used date to a variable when opening the DatePicker UI
		 */
		datePicker.setOnShown {
			lastDate = datePicker.value
		}

		/**
		 * Commits a change and closes the UI if the user picks a new date
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
		 * The JavaFX platform has a bug (fixed in Java 9), which does not commit value of its TextField when pressing the ENTER key
		 * This fixes the problem, it overrides default behaviour when pressing the ENTER key in the TextField
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