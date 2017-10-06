package com.duno.workmanager.Controllers

import com.duno.workmanager.Data.DataHolder
import com.duno.workmanager.Models.WorkSession
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.KeyCode
import javafx.util.Callback
import java.net.URL
import java.util.*

// TODO: Connect hourly wage to the controller
// TODO: Implement Time, Duration and Description cell value factories
// TODO: Implement custom date cell factory

class TableViewController : Initializable {
    @FXML lateinit var table: TableView<WorkSession>
    @FXML lateinit var date: TableColumn<WorkSession, String>
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
            if (it.isControlDown && it.code == KeyCode.N) {
                createNewRow()
            }

            if (it.code == KeyCode.DELETE) {
                if (table.selectionModel.selectedItem != null) {
                    removeRow(table.selectionModel.selectedItem)
                }
            }
        }
    }

    private fun commitHandlers() {
        date.onEditCommit = EventHandler { it.rowValue.beginDateProperty.setCzechString(it) }
//        time.onEditCommit = EventHandler { it.rowValue.beginTimeString = it.newValue }
//        durationProperty.onEditCommit = EventHandler { it.rowValue.durationString = it.newValue }
        description.onEditCommit = EventHandler { it.rowValue.descriptionProperty.value = it.newValue }
    }

    /**
     * Responsible for rendering the data contained within each cell for a single column
     */
    private fun cellFactories() {
        date.cellFactory = TextFieldTableCell.forTableColumn()
        time.cellFactory = TextFieldTableCell.forTableColumn()
        duration.cellFactory = TextFieldTableCell.forTableColumn()
        description.cellFactory = TextFieldTableCell.forTableColumn()
    }

    /**
     * Responsible for populating the data for all cells within a single column
     */
    private fun cellValueFactories() {
        date.cellValueFactory = Callback { it.value.beginDateProperty.getCzechString() }
        time.cellValueFactory = Callback { it.value.beginTimeProperty.asString() }
        duration.cellValueFactory = Callback { it.value.durationProperty.asString() }
        description.cellValueFactory = Callback { it.value.descriptionProperty }
    }

    /**
     * Double-click on blank space creates a newFile row
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
     * Creates newFile row with some data, scrolls to the end of the table and selects it
     */
    fun createNewRow() {
        val implicit = WorkSession(addMinutes = 30, hourlyWage = 0, description = "Doplnit!")
        val lastSession = table.items.lastOrNull() ?: implicit
        val session = WorkSession(addMinutes = 30, hourlyWage = lastSession.hourlyWageProperty.get(), description = lastSession.descriptionProperty.get())
        table.items.add(session)
        Platform.runLater {
            table.scrollTo(table.items.last())
            table.selectionModel.select(table.items.last())
        }
    }
}