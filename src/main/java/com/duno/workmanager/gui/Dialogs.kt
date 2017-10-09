package com.duno.workmanager.gui

import com.duno.workmanager.Main
import com.duno.workmanager.controllers.ExportDialogController
import com.duno.workmanager.data.DataHolder
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Window
import javafx.util.Callback
import java.io.File

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
 * Shows about dialog with GitHub link
 */
fun aboutDialog(parentWindow: Window) {
    val link = Hyperlink("GitHub")
    link.onAction = EventHandler {
        DataHolder.services?.showDocument("https://github.com/zvirdaniel/Work-Manager")
    }

    val flow = TextFlow(Text("Chyby pište na"), link)

    val alert = Alert(Alert.AlertType.INFORMATION)
    alert.title = "Autor"
    alert.headerText = "Daniel Zvir"
    alert.dialogPane.contentProperty().set(flow)

    parentWindow.let { alert.initOwner(it) }
    alert.initModality(Modality.WINDOW_MODAL)

    alert.show()
}