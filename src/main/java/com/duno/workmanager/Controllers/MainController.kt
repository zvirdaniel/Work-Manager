package com.duno.workmanager.Controllers

import com.duno.workmanager.Data.CurrentFile
import com.duno.workmanager.Data.FileManagement
import com.duno.workmanager.Data.VisibleData
import com.duno.workmanager.Other.aboutDialog
import com.duno.workmanager.Other.errorDialog
import com.duno.workmanager.Other.saveChooser
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.MenuItem
import javafx.scene.control.TabPane
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.util.Duration
import org.controlsfx.control.Notifications
import java.io.File
import java.net.URL
import java.time.ZoneId
import java.util.*


/**
 * Created by Daniel Zvir on 13.08.2017.
 */
class MainController : Initializable {
    @FXML lateinit var tabPane: TabPane
    @FXML lateinit var openFileMenu: MenuItem
    @FXML lateinit var saveFileMenu: MenuItem
    @FXML lateinit var saveAsFileMenu: MenuItem
    @FXML lateinit var newFileMenu: MenuItem
    @FXML lateinit var aboutMenu: MenuItem
    @FXML lateinit var deleteButton: Button
    @FXML lateinit var newRowButton: Button

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // EventHandlers
        newFileMenu.onAction = EventHandler { newFile() }
        openFileMenu.onAction = EventHandler { openFile() }
        saveFileMenu.onAction = EventHandler { saveFile() }
        saveAsFileMenu.onAction = EventHandler { saveFileAs() }
        deleteButton.onAction = EventHandler { deleteRow() }
        newRowButton.onAction = EventHandler { newRow() }
        aboutMenu.onAction = EventHandler { aboutDialog() }

        // Select current month
        tabPane.selectionModel.select(Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().month.value - 1)
    }


    private fun newRow() {
        val currentTabIndex = tabPane.selectionModel.selectedIndex
        val currentTab = VisibleData.getTabController(currentTabIndex)
        currentTab.newRow()
    }

    private fun deleteRow() {
        val currentTabIndex = tabPane.selectionModel.selectedIndex
        val currentTab = VisibleData.getTabController(currentTabIndex)
        val currentRow = currentTab.table.selectionModel.selectedItem
        if (currentRow != null) {
            currentTab.deleteRow(currentRow)
        }
    }

    private fun newFile() {
        var file = saveChooser("Vytvořit nový soubor",
                filters = listOf(ExtensionFilter("JSON", "*.json")))

        if (file != null) {
            if (!file.name.endsWith(".json")) {
                val newFile = File(file.path.plus(".json"))
                file = newFile
            }

            try {
                file.createNewFile()
            } catch (e: Exception) {
                errorDialog("Zde nelze zapsat soubor!")
                return
            }

            FileManagement.new(file)
        }
    }

    private fun saveFile() {
        if (FileManagement.save()) {
            Notifications.create()
                    .title("WorkManager")
                    .text("Soubor uložen jako ${CurrentFile.get().name}")
                    .hideAfter(Duration(4000.0))
                    .showInformation()
        }
    }

    private fun saveFileAs() {
        val originalFile = CurrentFile.get()

        var file = saveChooser(title = "Uložit soubor jako...",
                filters = listOf(ExtensionFilter("JSON", "*.json")),
                initialDir = File(originalFile.parent),
                initialFileName = originalFile.nameWithoutExtension + " kopie"
        )


        if (file != null) {
            if (!file.name.endsWith(".json")) {
                val newFile = File(file.path.plus(".json"))
                file = newFile
            }

            try {
                file.createNewFile()
            } catch (e: Exception) {
                errorDialog("Zde nelze zapsat soubor!")
                return
            }

            if (FileManagement.saveAs(file)) {
                Notifications.create()
                        .title("WorkManager")
                        .text("Soubor byl uložen jako ${file.name}")
                        .hideAfter(Duration(4000.0))
                        .showInformation()
            }
        }
    }

    private fun openFile() {
        val chooser = FileChooser()
        chooser.title = "Otevřít soubor"
        chooser.initialDirectory = File(System.getProperty("user.home"))
        chooser.getExtensionFilters().addAll(ExtensionFilter("JSON", "*.json"))
        val file = chooser.showOpenDialog(null)

        if (file != null) {
            FileManagement.open(file)
        }
    }
}