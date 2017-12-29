package cz.zvird.workmanager.gui

import cz.zvird.workmanager.Main
import cz.zvird.workmanager.controllers.ExportDialogController
import cz.zvird.workmanager.data.DataHolder
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
import java.time.Year

/**
 * Shows text input dialog to enter a year
 * @return integer, or null if user entered wrong values
 */
fun showYearSelectorDialog(ownerWindow: Window?): Int? {
	val dialog = TextInputDialog(Year.now().value.toString())
	dialog.title = "Zadejte rok"
	dialog.headerText = "Zadejte rok"
	dialog.contentText = "Prosím zadejte rok:"

	ownerWindow?.let { dialog.initOwner(it) }
	dialog.initModality(Modality.WINDOW_MODAL)

	val result = dialog.showAndWait()

	var year: Int? = null
	result.ifPresent {
		year = it.toIntOrNull()
	}

	return year
}

/**
 * @return range of months to export, selected xlsx file to export data into
 */
fun showExportFileDialog(ownerWindow: Window): Pair<IntRange, File?> {
	val loader = FXMLLoader(Main::class.java.getResource("views/ExportDialog.fxml"))
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
	dialog.initOwner(ownerWindow)
	dialog.initModality(Modality.WINDOW_MODAL)

	dialog.resultConverter = Callback {
		controller.getResult()
	}

	return dialog.showAndWait().get()
}

/**
 * Shows an open file dialog
 * @param title file chooser title
 * @param filters file extension filters
 * @param initialDir opened when chooser dialog opens
 * @param ownerWindow given to file chooser itself as owner
 * @return selected file
 */
fun showOpenFileDialog(
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
 * Shows a save file dialog
 * @param title file chooser title
 * @param filters file extension filters
 * @param initialDir opened when chooser dialog opens
 * @param initialFileName used as suggested file name
 * @param ownerWindow given to file chooser itself as owner
 * @param extension will be added to the file name, if not already there
 * @return selected file
 */
fun showSaveFileDialog(
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
 * Shows about dialog with a GitHub link
 */
fun showAboutDialog(ownerWindow: Window) {
	val link = Hyperlink("GitHub")
	link.onAction = EventHandler {
		DataHolder.services?.showDocument("https://github.com/zvirdaniel/Work-Manager")
	}

	val flow = TextFlow(Text("Chyby pište na"), link)

	val alert = Alert(Alert.AlertType.INFORMATION)
	alert.title = "Autor"
	alert.headerText = "Daniel Zvir"
	alert.dialogPane.content = flow

	ownerWindow.let { alert.initOwner(it) }
	alert.initModality(Modality.WINDOW_MODAL)

	alert.show()
}