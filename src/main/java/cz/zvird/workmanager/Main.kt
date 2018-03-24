package cz.zvird.workmanager

import cz.zvird.workmanager.data.DataHolder
import cz.zvird.workmanager.data.FileManager
import cz.zvird.workmanager.data.MemoryManager
import cz.zvird.workmanager.gui.BlockedTask
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

// TODO: Inspect current usage of threads, and possible future usages of it across the project (XLSX exporting)
// TODO: Start session and stop session buttons
// TODO: Implement Wage Calculator into the app
// TODO: Make tax calculation variables transparent and editable
// TODO: Inspect occasional fatal application failures when using safeCalls (too many Platform.runLater calls)

class Main : Application() {
	override fun start(stage: Stage) {
		DataHolder.primaryStage = stage
		DataHolder.services = hostServices // Used to open a hyperlink via the default browser in the about dialog

		loadIcons()

		val loader = FXMLLoader(javaClass.getResource("views/MainView.fxml"))
		val root = loader.load<Parent>()
		val css = javaClass.getResource("views/Main.css").toExternalForm()
		root.stylesheets.add(css)

		DataHolder.primaryStage.scene = Scene(root, 960.0, 600.0)
		DataHolder.primaryStage.minWidth = 960.0
		DataHolder.primaryStage.minHeight = 600.0
		DataHolder.primaryStage.title = DataHolder.appTitle
		DataHolder.primaryStage.show()

		initializeData()
	}

	/**
	 * Loads last used file, or creates and loads a temporary one
	 */
	private fun initializeData() {
		BlockedTask {
			try {
				MemoryManager.fileRefresh(true)
			} catch (e: Exception) {
				val temporaryFile = FileManager.new()
				FileManager.load(temporaryFile)
			}
		}
	}

	/**
	 * Loads icons of all sizes, JavaFX automatically selects the correct icon for the OS
	 */
	private fun loadIcons() {
		DataHolder.primaryStage.icons.addAll(
				Image(javaClass.getResource("icons/16x16.png").toURI().toString(), 16.0, 16.0, true, true),
				Image(javaClass.getResource("icons/20x20.png").toURI().toString(), 20.0, 20.0, true, true),
				Image(javaClass.getResource("icons/24x24.png").toURI().toString(), 24.0, 24.0, true, true),
				Image(javaClass.getResource("icons/32x32.png").toURI().toString(), 32.0, 32.0, true, true),
				Image(javaClass.getResource("icons/48x48.png").toURI().toString(), 48.0, 48.0, true, true),
				Image(javaClass.getResource("icons/64x64.png").toURI().toString(), 64.0, 64.0, true, true),
				Image(javaClass.getResource("icons/128x128.png").toURI().toString(), 128.0, 128.0, true, true),
				Image(javaClass.getResource("icons/256x256.png").toURI().toString(), 256.0, 256.0, true, true),
				Image(javaClass.getResource("icons/512x512.png").toURI().toString(), 512.0, 512.0, true, true)
		)
	}
}

fun main(args: Array<String>) {
	Application.launch(Main::class.java, *args)
}

/**
 * Checks if the current thread is equal to the JavaFX thread, and if not, wraps the function call in the Platform.runLater call
 * @param function to execute
 * @return value returned by the function
 */
fun <T> safeCall(function: () -> T): T? {
	if (Platform.isFxApplicationThread()) {
		return function()
	}

	var result: T? = null
	Platform.runLater {
		println("DEBUG: runLater has been used in the safeCall")
		result = function()
	}

	return result
}