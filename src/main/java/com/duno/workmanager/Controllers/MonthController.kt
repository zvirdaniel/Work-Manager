package com.duno.workmanager.Controllers
import com.duno.workmanager.Data.VisibleData
import com.duno.workmanager.Models.ObservableSession
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
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
        date.cellValueFactory = PropertyValueFactory<ObservableSession, String>("beginDateProperty")
        time.cellValueFactory = PropertyValueFactory<ObservableSession, String>("beginTimeProperty")
        duration.cellValueFactory = PropertyValueFactory<ObservableSession, String>("durationProperty")
        description.cellValueFactory = PropertyValueFactory<ObservableSession, String>("descriptionProperty")

        description.cellFactory = TextFieldTableCell.forTableColumn()
        description.onEditCommit = EventHandler {
            it.rowValue.descriptionProperty = it.newValue
        }

        VisibleData.addTab(this)
    }
}