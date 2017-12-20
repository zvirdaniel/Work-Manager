package cz.zvird.workmanager.controllers

import cz.zvird.workmanager.data.*
import cz.zvird.workmanager.gui.*
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
        newFileMenu.onAction = EventHandler { newFileDialog() }
        openFileMenu.onAction = EventHandler { openFileDialog() }
        saveFileMenu.onAction = EventHandler { saveFileNotificator() }
        saveAsFileMenu.onAction = EventHandler { saveFileAsDialog() }
        deleteButton.onAction = EventHandler { deleteRow() }
        newRowButton.onAction = EventHandler { newRow() }
        aboutMenu.onAction = EventHandler { aboutDialog(window) }
        exportMenu.onAction = EventHandler { exportDialog() }

        // MaskerPane is used to block the UI if needed
        stackPane.children.add(DataHolder.maskerPane)

        // If a tab is selected, request focus to the table within it, and scroll to the bottom
        tabPane.selectionModel.selectedIndexProperty().addListener { _, _, newValue ->
            val tableViewController = DataHolder.getTableViewController(newValue.toInt())
            Platform.runLater {
                tableViewController.table.requestFocus()
                if (tableViewController.table.items.isNotEmpty()) {
                    tableViewController.table.scrollTo(tableViewController.table.items.count() - 1)
                }
            }
        }

        // Select tab with current month
        tabPane.selectionModel.select(Date().toInstant().atZone(DataHolder.zone).toLocalDate().month.value - 1)

        Platform.runLater { window = tabPane.scene.window }
    }

    /**
     * Opens an export dialog (file selector, and month range selector) and calls the backend function
     */
    private fun exportDialog() {
        val pair = exportDialog(window)
        val monthRange = pair.first
        val file = pair.second

        if (file != null) {
            val blockedTask = object : BlockedTask<Unit>({ exportToSpreadsheet(monthRange, file) }) {
                override fun succeeded() {
                    savedAsNotification(file.name)
                }

                override fun failed() {
                    cantSaveNotification(file.name)
                }
            }

            thread { blockedTask.run() }
        }
    }

    /**
     * Creates a newFile row in currently opened tab by calling it's controller
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
    private fun newFileDialog() {
        val file = saveChooser("Vytvořit nový soubor",
                filters = listOf(ExtensionFilter("JSON", "*.json")),
                extension = ".json",
                ownerWindow = window)

        if (file != null) {
            try {
                newFile(file)
                savedAsNotification(file.name)
            } catch (e: Exception) {
                cantSaveNotification(file.name)
            }
        }
    }

    /**
     * Saves the file and notifies the user
     */
    private fun saveFileNotificator() {
        try {
            saveFile()
            savedAsNotification(CurrentFile.get().name)
        } catch (e: Exception) {
            cantSaveNotification(CurrentFile.get().name)
        }
    }

    /**
     * Opens a file selector, and calls the backend function
     */
    private fun saveFileAsDialog() {
        val originalFile = CurrentFile.get()

        val file = saveChooser(title = "Uložit soubor jako...",
                filters = listOf(ExtensionFilter("JSON", "*.json")),
                initialDir = File(originalFile.parent),
                initialFileName = originalFile.nameWithoutExtension + " kopie",
                extension = ".json",
                ownerWindow = window
        )


        if (file != null) {
            try {
                saveFileAs(file)
                savedAsNotification(file.name)
            } catch (e: Exception) {
                cantSaveNotification(file.name)
            }
        }
    }

    /**
     * Opens a file selector in home directory, calls backend for opening the file itself
     */
    private fun openFileDialog() {
        val file = openChooser(
                title = "Otevřít soubor",
                filters = listOf(ExtensionFilter("JSON", "*.json")),
                ownerWindow = window
        )

        if (file != null) {
            try {
                openFile(file)
            } catch (e: Exception) {
                Notifications.create()
                        .title("WorkManager")
                        .text("Soubor nelze otevřít, nebo není validní.")
                        .hideAfter(Duration(4000.0))
                        .showInformation()
            }
        }
    }
}