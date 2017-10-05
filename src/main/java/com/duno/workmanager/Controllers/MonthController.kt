package com.duno.workmanager.Controllers

import com.duno.workmanager.Data.VisibleData
import com.duno.workmanager.Data.WorkSession
import com.duno.workmanager.Models.ObservableSession
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.KeyCode
import javafx.util.Callback
import java.net.URL
import java.util.*

class MonthController : Initializable {
    @FXML
    var table = TableView<ObservableSession>()
    @FXML
    var date = TableColumn<ObservableSession, String>()
    @FXML
    var time = TableColumn<ObservableSession, String>()
    @FXML
    var duration = TableColumn<ObservableSession, String>()
    @FXML
    var description = TableColumn<ObservableSession, String>()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        table.placeholder = Label("Žádná data k zobrazení. Lze přidat tlačítkem dole, nebo Ctrl + N.")
        keyHandler()
        blankRowCallback()
        cellValueFactories()
        cellFactories()
        commitHandlers()
        VisibleData.addTabController(this)
    }

    private fun keyHandler() {
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
        date.onEditCommit = EventHandler { it.rowValue.beginDateString = it.newValue }
        time.onEditCommit = EventHandler { it.rowValue.beginTimeString = it.newValue }
        duration.onEditCommit = EventHandler { it.rowValue.durationString = it.newValue }
        description.onEditCommit = EventHandler { it.rowValue.descriptionString = it.newValue }
    }

    private fun cellFactories() {
        date.cellFactory = TextFieldTableCell.forTableColumn()
        time.cellFactory = TextFieldTableCell.forTableColumn()
        duration.cellFactory = TextFieldTableCell.forTableColumn()
        description.cellFactory = TextFieldTableCell.forTableColumn()
    }

    private fun cellValueFactories() {
        date.setCellValueFactory { it.value.beginDateProperty }
        time.setCellValueFactory { it.value.beginTimeProperty }
        duration.setCellValueFactory { it.value.durationProperty }
        description.setCellValueFactory { it.value.descriptionProperty }
    }

    private fun blankRowCallback() {
        table.rowFactory = Callback {
            val row = TableRow<ObservableSession>()

            row.onMouseClicked = EventHandler {
                if (it.clickCount >= 2 && row.item !is ObservableSession) {
                    createNewRow()
                }
            }

            row
        }
    }

    fun removeRow(row: ObservableSession) {
        table.items.remove(row)
    }

    fun createNewRow() {
        val lastSession = table.items.lastOrNull() ?: WorkSession(addMinutes = 30, hourlyWage = 0, description = "Doplnit!")
        val session = WorkSession(addMinutes = 30, hourlyWage = lastSession.hourlyWage, description = lastSession.description)
        table.items.add(ObservableSession(session))
        table.selectionModel.selectLast()
    }
}