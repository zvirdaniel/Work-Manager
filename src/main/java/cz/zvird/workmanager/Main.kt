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
		DataHolder.primaryStage.show()

		initializeData()

		DataHolder.getTableViewController().table.requestFocus()
		Platform.runLater { DataHolder.getTableViewController().table.selectionModel.selectFirst() }
	}

	/**
	 * Loads last used file, or creates and loads a temporary one, after the UI is loaded
	 */
	private fun initializeData() {
		Platform.runLater {
			BlockedTask {
				try {
					MemoryManager.fileRefresh(true)
				} catch (e: Exception) {
					val temporaryFile = FileManager.new()
					FileManager.load(temporaryFile)
				}
			}.run()

			DataHolder.mainController.refreshBottomBarUI()
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
