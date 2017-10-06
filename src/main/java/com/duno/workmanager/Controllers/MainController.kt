package com.duno.workmanager.Controllers

import com.duno.workmanager.Data.CurrentFile
import com.duno.workmanager.Data.DataHolder
import com.duno.workmanager.Data.FileManagement
import com.duno.workmanager.Data.exportToSpreadsheet
import com.duno.workmanager.Other.*
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.MenuItem
import javafx.scene.control.TabPane
import javafx.scene.control.TextField
import javafx.scene.layout.StackPane
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Window
import javafx.util.Duration
import org.controlsfx.control.Notifications
import java.io.File
import java.net.URL
import java.time.ZoneId
import java.util.*
import kotlin.concurrent.thread

/**
 * Controller for the main GUI, not containing TableView
 */
class MainController : Initializable {
    @FXML lateinit var tabPane: TabPane
    @FXML lateinit var openFileMenu: MenuItem
    @FXML lateinit var saveFileMenu: MenuItem
    @FXML lateinit var saveAsFileMenu: MenuItem
    @FXML lateinit var newFileMenu: MenuItem
    @FXML lateinit var aboutMenu: MenuItem
    @FXML lateinit var exportMenu: MenuItem
    @FXML lateinit var deleteButton: Button
    @FXML lateinit var newRowButton: Button
    @FXML lateinit var stackPane: StackPane
    @FXML lateinit var hourlyWageField: TextField
    private lateinit var window: Window

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // EventHandlers
        newFileMenu.onAction = EventHandler { newFile() }
        openFileMenu.onAction = EventHandler { openFile() }
        saveFileMenu.onAction = EventHandler { saveFile() }
        saveAsFileMenu.onAction = EventHandler { saveFileAs() }
        deleteButton.onAction = EventHandler { deleteRow() }
        newRowButton.onAction = EventHandler { newRow() }
        aboutMenu.onAction = EventHandler { aboutDialog(window) }
        exportMenu.onAction = EventHandler { exportData() }

        // MaskerPane is used to block the UI if needed
        stackPane.children.add(DataHolder.maskerPane)

        // Select current month, focus table if tab is clicked
        tabPane.selectionModel.select(Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().month.value - 1)
        tabPane.selectionModel.selectedIndexProperty().addListener({ _, _, newValue ->
            val tab = DataHolder.getTableViewController(newValue.toInt())
            Platform.runLater { tab.table.requestFocus() }
        })

        Platform.runLater { window = tabPane.scene.window }
    }

    /**
     * Opens an export dialog (file selector, and month range selector) and calls the backend function
     */
    private fun exportData() {
        val pair = exportDialog(window)
        val monthRange = pair.first
        val file = pair.second

        if (file != null) {
            val blockedTask = object : ProgressTask<Boolean>({ exportToSpreadsheet(monthRange, file) }) {
                override fun succeeded() {
                    notifySavedAs(file.name)
                }

                override fun failed() {
                    notifyCantSave(file.name)
                }
            }

            DataHolder.maskerPane.progressProperty().bind(blockedTask.progressProperty())
            DataHolder.maskerPane.textProperty().bind(blockedTask.messageProperty())
            DataHolder.maskerPane.visibleProperty().bind(blockedTask.runningProperty())
            thread { blockedTask.run() }
        }
    }

    /**
     * Creates a new row in currently opened tab by calling it's controller
     */
    private fun newRow() {
        val currentTabIndex = tabPane.selectionModel.selectedIndex
        val currentTab = DataHolder.getTableViewController(currentTabIndex)
        currentTab.createNewRow()
    }

    /**
     * Deletes a row by calling the controller for currently opened tab
     */
    private fun deleteRow() {
        val currentTabIndex = tabPane.selectionModel.selectedIndex
        val currentTab = DataHolder.getTableViewController(currentTabIndex)
        val currentRow = currentTab.table.selectionModel.selectedItem
        if (currentRow != null) {
            currentTab.removeRow(currentRow)
        }
    }

    /**
     * Opens a file selector and calls the backend function
     */
    private fun newFile() {
        val file = saveChooser("Vytvořit nový soubor",
                filters = listOf(ExtensionFilter("JSON", "*.json")),
                extension = ".json",
                ownerWindow = window)

        if (file != null) {
            if (FileManagement.new(file)) {
                notifySavedAs(file.name)
            } else {
                notifyCantSave(file.name)
            }
        }
    }

    /**
     * Calls backend's save function, creates a notification
     */
    private fun saveFile() {
        if (FileManagement.save()) {
            notifySavedAs(CurrentFile.get().name)
        } else {
            notifyCantSave(CurrentFile.get().name)
        }
    }

    /**
     * Opens a file selector, and calls the backend function
     */
    private fun saveFileAs() {
        val originalFile = CurrentFile.get()

        val file = saveChooser(title = "Uložit soubor jako...",
                filters = listOf(ExtensionFilter("JSON", "*.json")),
                initialDir = File(originalFile.parent),
                initialFileName = originalFile.nameWithoutExtension + " kopie",
                extension = ".json",
                ownerWindow = window
        )


        if (file != null) {
            if (FileManagement.saveAs(file)) {
                notifySavedAs(file.name)
            } else {
                notifyCantSave(file.name)
            }
        }
    }

    /**
     * Opens a file selector in home directory, calls backend for opening the file itself
     */
    private fun openFile() {
        val file = openChooser(
                title = "Otevřít soubor",
                filters = listOf(ExtensionFilter("JSON", "*.json")),
                ownerWindow = window
        )

        if (file != null) {
            if (!FileManagement.open(file)) {
                Notifications.create()
                        .title("WorkManager")
                        .text("Soubor nelze otevřít, nebo není validní.")
                        .hideAfter(Duration(4000.0))
                        .showInformation()
            }
        }
    }
}