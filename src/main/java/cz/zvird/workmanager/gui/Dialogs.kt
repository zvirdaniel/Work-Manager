package cz.zvird.workmanager.gui

import cz.zvird.workmanager.Main
import cz.zvird.workmanager.controllers.ExportDialogController
import cz.zvird.workmanager.data.DataHolder
import cz.zvird.workmanager.safeCall
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
fun showYearSelectorDialog(ownerWindow: Window?, headerText: String? = null): Int? = safeCall {
	val dialog = TextInputDialog(Year.now(DataHolder.zone).value.toString())
	dialog.title = "Zadejte rok"
	dialog.headerText = headerText ?: "Zadejte rok"
	dialog.contentText = "Prosím zadejte rok:"

	ownerWindow?.let { dialog.initOwner(it) }
	dialog.initModality(Modality.WINDOW_MODAL)

	val result = dialog.showAndWait()

	var year: Int? = null
	result?.ifPresent {
		year = it.toIntOrNull()
	}

	year // return here
}

/**
 * @return null if dialog was canceled, Pair with range of months to export and the selected file otherwise
 */
fun showExportFileDialog(ownerWindow: Window): Pair<IntRange, File?>? = safeCall {
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
	val cancelButtonType = ButtonType("Zrušit", ButtonBar.ButtonData.CANCEL_CLOSE)
	dialog.dialogPane.buttonTypes.addAll(cancelButtonType, exportButtonType)

	// Pass export button to controller, button can be pressed only after a file has been selected
	val exportButton = dialog.dialogPane.lookupButton(exportButtonType)
	controller.exportButton = exportButton
	exportButton.disableProperty().set(true)

	// Set content from FXML
	dialog.dialogPane.content = content
	dialog.dialogPane.minWidth = 400.0
	dialog.initOwner(ownerWindow)
	dialog.initModality(Modality.WINDOW_MODAL)

	// Callback handles the values returned by the dialog
	dialog.resultConverter = Callback {
		if (it == exportButtonType) {
			controller.getResult()
		} else {
			null
		}
	}

	val result = dialog.showAndWait()
	if (result.isPresent) {
		result.get() // return here
	} else {
		null // return here
	}
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
): File? = safeCall {
	val chooser = FileChooser()
	chooser.title = title
	chooser.initialDirectory = initialDir

	if (filters != null) {
		chooser.extensionFilters.addAll(filters)
	}

	chooser.showOpenDialog(ownerWindow) // return here
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
): File? = safeCall {
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

	file // return here
}

/**
 * Shows about dialog with a GitHub link
 */
fun showAboutDialog(ownerWindow: Window) {
	safeCall {
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
}