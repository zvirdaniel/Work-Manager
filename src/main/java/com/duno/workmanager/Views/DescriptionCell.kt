package com.duno.workmanager.Views

import com.duno.workmanager.Models.ObservableSession
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TableCell


class DescriptionCell : TableCell<ObservableSession, String>() {
    var buySellBox = ChoiceBox<String>()

    init {
        buySellBox.items.addAll("Buy", "Sell")
        buySellBox.selectionModel.selectedIndexProperty().addListener { obs, oldValue, newValue ->
            val value = buySellBox.items[newValue.toInt()]
            processEdit(value)
        }
    }

    private fun processEdit(value: String) {
        commitEdit(value)
    }

    override fun cancelEdit() {
        super.cancelEdit()
        text = item
        graphic = null
    }

    override fun commitEdit(value: String?) {
        super.commitEdit(value)
        // ((Item) this.getTableRow().getItem()).setName(value);
        graphic = null
    }

    override fun startEdit() {
        super.startEdit()
        val value = item
        if (value != null) {
            graphic = buySellBox
            text = null
        }
    }

    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            text = null

        } else {
            text = item
        }
    }

}