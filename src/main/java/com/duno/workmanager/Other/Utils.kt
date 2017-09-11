package com.duno.workmanager.Other

import com.duno.workmanager.Controllers.ExportDialogController
import com.duno.workmanager.Data.CurrentFile
import com.duno.workmanager.Main
import javafx.application.HostServices
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Hyperlink
import javafx.scene.layout.AnchorPane
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.FileChooser
import javafx.stage.Window
import org.controlsfx.control.Notifications
import java.io.File
import java.time.ZoneId
import java.util.*

var services: HostServices? = null

/**
 * @return range of months to export, file to export data into
 */
fun exportDialog(): Pair<IntRange, File> {
    // TODO: Open file chooser to select file
    val loader = FXMLLoader(Main::class.java.getResource("Views/ExportDialog.fxml"))
    loader.setController(ExportDialogController())
    val content = loader.load<AnchorPane>()

    // Create the custom dialog
    val dialog = Dialog<IntRange>()
    dialog.setTitle("Export dat do tabulky")
    dialog.setHeaderText("Vyberte data k exportu do tabulky")

    // Set the buttons
    dialog.getDialogPane().getButtonTypes().
            addAll(ButtonType("Export", ButtonData.OK_DONE), ButtonType.CANCEL)

    // Do some validation (using the Java 8 lambda syntax)
    // username.textProperty().addListener({ observable, oldValue, newValue -> exportButton.setDisable(newValue.trim().isEmpty()) })

    dialog.dialogPane.content = content
    dialog.dialogPane.minWidth = 400.0

    // Platform.runLater({ username.requestFocus() })

    val result: Optional<IntRange> = dialog.showAndWait()

    return Pair(1..1, CurrentFile.get())
}

// TODO: Set file owners

/**
 * @param title file chooser title
 * @param filters file extension filters
 * @param initialDir opened when chooser dialog opens
 * @param initialFileName used as suggested file name
 * @param ownerWindow given to file chooser itself as owner
 */
fun saveChooser(
        title: String,
        filters: Collection<FileChooser.ExtensionFilter>? = null,
        initialDir: File = File(System.getProperty("user.home")),
        initialFileName: String? = null,
        ownerWindow: Window? = null
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

    return chooser.showSaveDialog(ownerWindow)
}

/**
 * Shows error dialog with a given message
 */
fun errorDialog(message: String) {
    val alert = Alert(AlertType.ERROR)
    alert.title = "Chyba"
    alert.headerText = "Hele, chyba!"
    alert.contentText = message
    alert.showAndWait()
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
fun aboutDialog() {
    val link = Hyperlink("GitHub")
    link.onAction = EventHandler {
        services?.showDocument("https://github.com/zvirdaniel/Work-Manager")
    }

    val flow = TextFlow(Text("Chyby pište na"), link)

    val alert = Alert(AlertType.INFORMATION)
    alert.title = "Autor"
    alert.headerText = "Daniel Zvir"
    alert.dialogPane.contentProperty().set(flow)

    alert.showAndWait()
}