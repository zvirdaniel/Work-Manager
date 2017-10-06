package com.duno.workmanager.Models.Properties

import com.duno.workmanager.Models.WorkSession
import com.duno.workmanager.Other.errorNotification
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TableColumn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

/**
 * Allows custom getters and setter for the property
 */
class LocalDateProperty(date: LocalDate) : SimpleObjectProperty<LocalDate>(date) {
    /**
     * @return formatted czech string containing date
     */
    fun getCzechString(): StringBinding {
        val date = this.get()
        return object : StringBinding() {
            override fun computeValue(): String {
                return DateTimeFormatter.ofPattern("dd. MM. yyyy").format(date)
            }
        }
    }

    /**
     * Formats string into LocalDate and sets it, shows error notification otherwise
     */
    fun setCzechString(column: TableColumn.CellEditEvent<WorkSession, String>) {
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

        val newValue = column.newValue
        val newValueWithoutSpaces = newValue.replace("[\\s|\u00A0]+", "")
        val tableView = column.tableView

        try {
            val date: LocalDate
            if (IntRange(1, 3).contains(newValueWithoutSpaces.length) && tableView.items.isNotEmpty()) {
                val lastRow = tableView.items[column.tablePosition.row - 1]
                val year = lastRow.beginDateProperty.get().year
                val month = lastRow.beginDateProperty.get().month.value
                date = LocalDate.of(year, month, newValueWithoutSpaces.replace(".", "").toInt())
            } else {
                date = LocalDate.parse(newValueWithoutSpaces, formatter)
            }

            super.set(date)
            println("Begin date - $date was set.")
        } catch (e: Exception) {
            errorNotification("Chyba při parsování $newValue")
        } finally {
            tableView.refresh()
        }
    }
}