package com.duno.workmanager.Controllers
import com.duno.workmanager.Data.DataManagement
import com.duno.workmanager.Models.ObservableSession
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import java.net.URL
import java.util.*

/**
 * Created by Daniel Zvir on 26.7.17.
 */

// TODO: Async loading

class MonthController : Initializable {
    @FXML var table = TableView<ObservableSession>()
    @FXML var date = TableColumn<ObservableSession, String>()
    @FXML var time = TableColumn<ObservableSession, String>()
    @FXML var duration = TableColumn<ObservableSession, String>()
    @FXML var description = TableColumn<ObservableSession, String>()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        date.cellValueFactory = PropertyValueFactory<ObservableSession, String>("beginDate")
        time.cellValueFactory = PropertyValueFactory<ObservableSession, String>("beginTime")
        duration.cellValueFactory = PropertyValueFactory<ObservableSession, String>("duration")
        description.cellValueFactory = PropertyValueFactory<ObservableSession, String>("description")

        table.items.clear()
        table.items.addAll(DataManagement.sessionList)
    }
}