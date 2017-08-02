package com.duno.workmanager.Controllers

import com.duno.workmanager.Models.MonthModel
import com.duno.workmanager.getObservableSessions
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView

/**
 * Created by Daniel Zvir on 26.7.17.
 */
class MonthController {
    @FXML lateinit var table: TableView<MonthModel>

//    @FXML lateinit var dateColumn: TableColumn<MonthModel, Date>

    @FXML lateinit var beginTimeColumn: TableColumn<MonthModel, String>

    @FXML lateinit var hoursColumn: TableColumn<MonthModel, String>

    @FXML lateinit var descriptionColumn: TableColumn<MonthModel, String>

    @FXML
    fun initialize() {
        beginTimeColumn.setCellValueFactory { it.value.beginTime }
        hoursColumn.setCellValueFactory { it.value.duration }
        descriptionColumn.setCellValueFactory { it.value.description }

        table.items = getObservableSessions(5)
    }
}
