package com.duno.workmanager.Controllers

import com.duno.workmanager.Data.VisibleData
import com.duno.workmanager.Models.ObservableSession
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.TextFieldTableCell
import java.net.URL
import java.util.*


// TODO: Async loading
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
        date.setCellValueFactory { it.value.beginDateProperty }
        time.setCellValueFactory { it.value.beginTimeProperty }
        duration.setCellValueFactory { it.value.durationProperty }
        description.setCellValueFactory { it.value.descriptionProperty }

        date.cellFactory = TextFieldTableCell.forTableColumn()
        time.cellFactory = TextFieldTableCell.forTableColumn()
        duration.cellFactory = TextFieldTableCell.forTableColumn()
        description.cellFactory = TextFieldTableCell.forTableColumn()

        date.onEditCommit = EventHandler { it.rowValue.beginDateString = it.newValue }
        time.onEditCommit = EventHandler { it.rowValue.beginTimeString = it.newValue }
        duration.onEditCommit = EventHandler { it.rowValue.durationString = it.newValue }
        description.onEditCommit = EventHandler { it.rowValue.descriptionString= it.newValue }

        VisibleData.addTab(this)
    }
}