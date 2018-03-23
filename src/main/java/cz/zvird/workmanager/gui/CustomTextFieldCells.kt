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
private fun <S, T> generateCustomTextFieldTableCell(converter: StringConverter<T>): TextFieldTableCell<S, T> {
	return object : TextFieldTableCell<S, T>(converter) {
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
			 * The following code tells the row editor to continue editing the next cell
			 */
			if (oldValue == newValue) {
				DataHolder.editCellFinishNow = true
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
		override fun toString(duration: Duration): String {
			return duration.toString()
					.substring(2)
					.replace("(\\d[HMS])(?!$)".toRegex(), "$1 ")
					.toLowerCase()
		}

		override fun fromString(string: String): Duration {
			try {
				if (string.contains("h", true) || string.contains('.') || string.contains(',')) {
					var input = string

					// Possible values at this point are: 4,5h or 4.5h or 4h
					if (string.contains("h", true)) {
						input = input.substringBeforeLast('h').trim()
					}

					// Possible values at this point are: 4,5 or 4.5 or 4
					if (input.contains(',')) {
						input = input.replace(',', '.')
					}

					// Possible values at this point are: 4.5 or 4
					if (input.contains('.')) {
						val minutes = input.toDouble() * 60
						return Duration.ofMinutes(minutes.toLong())
					}

					val hours = input.toLong()
					return Duration.ofHours(hours)
				}

				val minutes = string.toLong()
				return Duration.ofMinutes(minutes)
			} catch (e: NumberFormatException) {
				errorNotification("$string není validní stup! Povolené formáty jsou:\n" +
						"150 => 150 minut\n" +
						"2h => 2 hodiny\n" +
						"2,5h => 2 hodiny 30 minut", 10000.0)
			}

			return Duration.ofMinutes(30)
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