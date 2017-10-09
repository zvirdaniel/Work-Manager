package com.duno.workmanager.gui

import com.duno.workmanager.models.WorkSession
import javafx.event.EventHandler
import javafx.scene.control.ContentDisplay
import javafx.scene.control.TableCell
import java.time.LocalDate

/**
 * Custom cell with a DatePicker
 */
class LocalDateCell : TableCell<WorkSession, LocalDate>() {
    private val datePicker = CzechDatePicker()

    init {
        // Cancel edit on focus loss
        datePicker.focusedProperty().addListener { _, _, isFocused ->
            if (!isFocused) {
                cancelEdit()
            }
        }

        // TODO: Commit values upon actual date selection (or enter button, if text input was used) instead of checking picker.onHidden
        // TODO: Find out why clicking on calendar in DatePicker twice trigggers focus lost event
        // Commit values when graphical date selector is closed
        datePicker.onHidden = EventHandler {
            if (datePicker.value != null) {
                commitEdit(datePicker.value)
            }
        }

        // Starts editing on double click
        onMouseClicked = EventHandler {
            if (it.clickCount == 2) {
                startEdit()
            }
        }
    }

    /**
     * Makes DatePicker visible
     */
    override fun startEdit() {
        super.startEdit()
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
    }

    /**
     * Hides DatePicker and commits the value to the controller
     */
    override fun commitEdit(newValue: LocalDate?) {
        super.commitEdit(newValue)
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
     * Updates TableCell with data
     */
    override fun updateItem(item: LocalDate?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            reset()
        } else {
            setGraphicBindText(item)
            contentDisplay = ContentDisplay.TEXT_ONLY // TEXT_ONLY should be set when not editing
        }
    }

    /**
     * Binds textProperty, which is visible when cell is unfocused, to date formatted as dd. mm. yyyy, and sets DatePicker as graphic.
     */
    private fun setGraphicBindText(item: LocalDate?) {
        datePicker.value = item
        textProperty().bind(datePicker.getCzechString())
        graphic = datePicker
    }

    /**
     * Unbinds textProperty, then resets text and graphic to null.
     */
    private fun reset() {
        textProperty().unbind()
        text = null
        graphic = null
        contentDisplay = ContentDisplay.TEXT_ONLY
    }
}