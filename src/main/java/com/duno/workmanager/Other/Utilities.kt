package com.duno.workmanager.Other

import com.duno.workmanager.Controllers.ExportDialogController
import com.duno.workmanager.Main
import javafx.application.HostServices
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.AnchorPane
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Window
import javafx.util.Callback
import javafx.util.Duration
import org.controlsfx.control.Notifications
import java.io.File
import java.time.ZoneId
import java.util.*

var services: HostServices? = null // Used in about dialog to open a link in a web browser

/**
 * @param body function reference
 */
open class ProgressTask<T>(val body: () -> T) : Task<T>() {
    override fun call(): T {
        updateMessage("Probíhá zpracování")

        val result = body()

        Thread.sleep(1000)
        updateMessage("Zpracování dokončeno")
        updateProgress(100, 100)

        return result
    }
}

/**
 * @return range of months to export, .xlsx file to export data into
 */
fun exportDialog(parentWindow: Window): Pair<IntRange, File?> {
    val loader = FXMLLoader(Main::class.java.getResource("Views/ExportDialog.fxml"))
    val controller = ExportDialogController()
    loader.setController(controller)
    val content = loader.load<AnchorPane>()

    // Create a custom dialog
    val dialog = Dialog<Pair<IntRange, File?>>()
    dialog.title = "Export dat do tabulky"
    dialog.headerText = "Vyberte data k exportu do tabulky"

    // Add buttons
    val exportButtonType = ButtonType("Export", ButtonBar.ButtonData.OK_DONE)
    dialog.dialogPane.buttonTypes.addAll(ButtonType.CANCEL, exportButtonType)

    // Pass export button to controller, button can be pressed only after a file has been selected
    val exportButton = dialog.dialogPane.lookupButton(exportButtonType)
    controller.exportButton = exportButton
    exportButton.disableProperty().set(true)

    // Set content from FXML
    dialog.dialogPane.content = content
    dialog.dialogPane.minWidth = 400.0
    dialog.initOwner(parentWindow)
    dialog.initModality(Modality.WINDOW_MODAL)

    dialog.resultConverter = Callback {
        controller.getResult()
    }

    return dialog.showAndWait().get()
}

/**
 * @param title file chooser title
 * @param filters file extension filters
 * @param initialDir opened when chooser dialog opens
 * @param ownerWindow given to file chooser itself as owner
 * @return selected file
 * Opens an open file dialog
 */
fun openChooser(
        title: String,
        filters: Collection<FileChooser.ExtensionFilter>? = null,
        initialDir: File = File(System.getProperty("user.home")),
        ownerWindow: Window? = null
): File? {
    val chooser = FileChooser()
    chooser.title = title
    chooser.initialDirectory = initialDir

    if (filters != null) {
        chooser.extensionFilters.addAll(filters)
    }

    return chooser.showOpenDialog(ownerWindow)
}

/**
 * @param title file chooser title
 * @param filters file extension filters
 * @param initialDir opened when chooser dialog opens
 * @param initialFileName used as suggested file name
 * @param ownerWindow given to file chooser itself as owner
 * @param extension will be added to the file name, if not already there
 * @return selected file
 * Opens a save file dialog
 */
fun saveChooser(
        title: String,
        filters: Collection<FileChooser.ExtensionFilter>? = null,
        initialDir: File = File(System.getProperty("user.home")),
        initialFileName: String? = null,
        ownerWindow: Window? = null,
        extension: String = ""
): File? {
    val chooser = FileChooser()
    chooser.title = title
    chooser.initialDirectory = initialDir

    if (filters != null) {
        chooser.extensionFilters.addAll(filters)
    }

    if (initialFileName != null) {
        chooser.initialFileName = initialFileName
    }

    var file = chooser.showSaveDialog(ownerWindow)

    if (file != null && extension.isNotEmpty()) {
        if (!file.name.endsWith(extension)) {
            file = File(file.path.plus(extension))
        }
    }

    return file
}

/**
 * @param fileName that will show up in the notification
 * Creates a notification informing the user that the fileName cant be saved
 */
fun notifyCantSave(fileName: String) {
    Notifications.create()
            .title("WorkManager")
            .text("Soubor nelze uložit jako $fileName.")
            .hideAfter(Duration(4000.0))
            .showError()
}

/**
 * @param fileName that will show up in the notification
 * Creates a notification informing the user about successful saving of the given file
 */
fun notifySavedAs(fileName: String) {
    Notifications.create()
            .title("WorkManager")
            .text("Soubor uložen jako $fileName.")
            .hideAfter(Duration(4000.0))
            .showInformation()
}

/**
 * Shows error notification with a given message
 */
fun errorNotification(message: String) {
    Notifications.create()
            .title("WorkManager")
            .text(message)
            .hideAfter(javafx.util.Duration(4000.0))
            .showError()
}

/**
 * @return true if dates are on the same day, does not compare time
 */
fun isOnSameDay(first: Date, second: Date): Boolean {
    val localFirst = first.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val localSecond = second.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    return localFirst.equals(localSecond)
}

/**
 * Shows about dialog with GitHub link
 */
fun aboutDialog(parentWindow: Window) {
    val link = Hyperlink("GitHub")
    link.onAction = EventHandler {
        services?.showDocument("https://github.com/zvirdaniel/Work-Manager")
    }

    val flow = TextFlow(Text("Chyby pište na"), link)

    val alert = Alert(AlertType.INFORMATION)
    alert.title = "Autor"
    alert.headerText = "Daniel Zvir"
    alert.dialogPane.contentProperty().set(flow)

    parentWindow.let { alert.initOwner(it) }
    alert.initModality(Modality.WINDOW_MODAL)

    alert.show()
}