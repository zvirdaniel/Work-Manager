package com.duno.workmanager.gui

import com.duno.workmanager.models.WorkSession
import javafx.beans.binding.StringBinding
import javafx.scene.control.DatePicker
import javafx.scene.control.TablePosition
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

// TODO: Custom text formatting
// TODO: Check text input for supported formats

class CzechDatePicker : DatePicker() {
    init {
        promptText = "dd. mm. yyyy"
        isEditable = true
    }

    /**
     * @return formatted czech string containing date
     */
    fun getCzechString(): StringBinding {
        val date = value
        return object : StringBinding() {
            override fun computeValue(): String {
                return DateTimeFormatter.ofPattern("dd. MM. yyyy").format(date)
            }
        }
    }

    /**
     * Formats string into LocalDate and sets it.
     * Accepted formats are:
     * dd. MM. yyyy
     * dd. M. yyyy
     * d. M. yyyy
     * dd - dot is voluntary, will use the month in the tab name
     * d - dot is voluntary, will use the month in the tab name
     * @throws IllegalArgumentException if format is incorrect
     */
    fun setCzechString(newValue: String, tablePosition: TablePosition<WorkSession, String>) {
        val formatter = DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .optionalStart()
                .appendPattern("dd. MM. yyyy")
                .optionalEnd()
                .optionalStart()
                .appendPattern("dd. M. yyyy")
                .optionalEnd()
                .optionalStart()
                .appendPattern("d. M. yyyy")
                .optionalEnd()
                .optionalStart()
                .appendPattern("d. MM. yyyy")
                .optionalEnd()
                .toFormatter()
        val value = newValue.replace("[\\s|\u00A0]+", "") // Removes all spacing
        val table = tablePosition.tableView

        try {
            val date: LocalDate
            date = if (IntRange(1, 3).contains(value.length) && table.items.isNotEmpty()) {
                val lastRow = table.items[tablePosition.row - 1]
                val year = lastRow.beginDateProperty.get().year
                val month = lastRow.beginDateProperty.get().month.value
                LocalDate.of(year, month, value.replace(".", "").toInt())
            } else {
                LocalDate.parse(value, formatter)
            }

            this.value = date
            println("Begin date - $date was set.")
        } catch (e: Exception) {
            throw IllegalArgumentException(value)
        }
    }
}