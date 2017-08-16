package com.duno.workmanager.Controllers

import com.duno.workmanager.Data.DataManagement
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.MenuItem
import javafx.scene.control.TabPane
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import java.net.URL
import java.time.ZoneId
import java.util.*


/**
 * Created by Daniel Zvir on 13.08.2017.
 */
class MainController : Initializable {
    @FXML lateinit var tabPane: TabPane
    @FXML lateinit var openFileMenu: MenuItem

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        openFileMenu.onAction = EventHandler { selectAndSetFile() }
        val currentMonth = Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().month
        tabPane.selectionModel.select(currentMonth.value - 1)
    }

    fun selectAndSetFile() {
        val chooser = FileChooser()
        chooser.getExtensionFilters().addAll(ExtensionFilter("JSON", "*.json"))
        val file = chooser.showOpenDialog(null)

        if (file != null) {
            DataManagement.setFile(file)
        }
    }
}