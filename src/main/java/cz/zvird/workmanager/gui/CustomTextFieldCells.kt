package cz.zvird.workmanager.gui

import cz.zvird.workmanager.data.DataHolder
import cz.zvird.workmanager.models.WorkSession
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.util.StringConverter
import javafx.util.converter.DefaultStringConverter
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Generates custom text field table cell, which integrates into the DataHolders variables properly
 * Handles edit cancellation, fixes the bug caused when committing the same value to the cell
 */
private fun <T> generateCustomTextFieldTableCell(converter: StringConverter<T>): TextFieldTableCell<WorkSession, T> {
	return object : TextFieldTableCell<WorkSession, T>(converter) {
		init {
			/**
			 * Informs the DataHolder about canceling the edit
			 */
			this.addEventFilter(KeyEvent.KEY_PRESSED) {
				if (!it.isShiftDown && !it.isControlDown && !it.isAltDown && it.code == KeyCode.ESCAPE) {
					DataHolder.editCellCancelNow = true
				}
			}
		}

		private var oldValue: T? = null

		override fun startEdit() {
			super.startEdit()
			oldValue = this.item
			DataHolder.editCellFinishNow = false
		}

		override fun commitEdit(newValue: T) {
			super.commitEdit(newValue)

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
	}
}

/**
 * Generates custom text field table cell with the default string converter
 */
fun generateStringTextCell(): TextFieldTableCell<WorkSession, String> = generateCustomTextFieldTableCell(DefaultStringConverter())

/**
 * Generates custom text field table cell with the duration string converter
 */
fun generateDurationTextCell(): TextFieldTableCell<WorkSession, Duration> {
	val durationConverter = object : StringConverter<Duration>() {
		var oldString = ""
		var oldDuration = Duration.ZERO

		override fun toString(duration: Duration): String {
			val newString = duration.toString()
					.substring(2)
					.replace("(\\d[HMS])(?!$)".toRegex(), "$1 ")
					.toLowerCase()

			oldString = newString
			oldDuration = duration
			return newString
		}

		override fun fromString(newString: String): Duration {
			if (oldString == newString) {
				return oldDuration
			}

			try {
				val minutes = newString.toLong() * 60.0
				return Duration.ofMinutes(minutes.toLong())
			} catch (e: NumberFormatException) {
				errorNotification("$newString není validní stup!")
			}

			return Duration.ZERO
		}
	}

	return generateCustomTextFieldTableCell(durationConverter)
}

/**
 * Generates custom text field table cell with the LocalTime string converter
 */
fun generateLocalTimeTextCell(): TextFieldTableCell<WorkSession, LocalTime> {
	val localTimeConverter = object : StringConverter<LocalTime>() {
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
	}

	return generateCustomTextFieldTableCell(localTimeConverter)
}