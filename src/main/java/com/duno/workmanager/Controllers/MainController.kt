package com.duno.workmanager.Controllers

import com.duno.workmanager.Data.CurrentFile
import com.duno.workmanager.Data.FileManagement
import com.duno.workmanager.Other.errorDialog
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
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

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // EventHandlers
        newFileMenu.onAction = EventHandler { newFile() }
        openFileMenu.onAction = EventHandler { openFile() }
        saveFileMenu.onAction = EventHandler { saveFile() }
        saveAsFileMenu.onAction = EventHandler { saveFileAs() }

        // Select current month
        tabPane.selectionModel.select(Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().month.value - 1)
    }

    private fun newFile() {
        val chooser = FileChooser()
        chooser.title = "Create new file"
        chooser.initialDirectory = File(System.getProperty("user.home"))
        chooser.extensionFilters.addAll(ExtensionFilter("JSON", "*.json"))
        var file = chooser.showSaveDialog(null)

        if (file != null) {
            if (!file.name.endsWith(".json")) {
                val newFile = File(file.path.plus(".json"))
                file = newFile
            }

            try {
                file.createNewFile()
            } catch (e: Exception) {
                errorDialog("Can't create file there!")
                return
            }

            FileManagement.new(file)
        }
    }

    private fun saveFile() {
        if (FileManagement.save()) {
            Notifications.create()
                    .title("WorkManager")
                    .text("File was saved as ${CurrentFile.get().name}")
                    .hideAfter(Duration(4000.0))
                    .showInformation()
        }
    }

    private fun saveFileAs() {
        val originalFile = CurrentFile.get()
        val chooser = FileChooser()
        chooser.title = "Save file as"
        chooser.initialFileName = originalFile.nameWithoutExtension + " copy"
        chooser.initialDirectory = File(originalFile.parent)
        chooser.extensionFilters.addAll(ExtensionFilter("JSON", "*.json"))
        var file = chooser.showSaveDialog(null)

        if (file != null) {
            if (!file.name.endsWith(".json")) {
                val newFile = File(file.path.plus(".json"))
                file = newFile
            }

            try {
                file.createNewFile()
            } catch (e: Exception) {
                errorDialog("Can't create file there!")
                return
            }

            if (FileManagement.saveAs(file)) {
                Notifications.create()
                        .title("WorkManager")
                        .text("File was saved as ${file.name}")
                        .hideAfter(Duration(4000.0))
                        .showInformation()
            }
        }
    }

    private fun openFile() {
        val chooser = FileChooser()
        chooser.title = "Open a file"
        chooser.initialDirectory = File(System.getProperty("user.home"))
        chooser.getExtensionFilters().addAll(ExtensionFilter("JSON", "*.json"))
        val file = chooser.showOpenDialog(null)

        if (file != null) {
            FileManagement.open(file)
        }
    }
}