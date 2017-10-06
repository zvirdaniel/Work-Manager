package com.duno.workmanager

import com.duno.workmanager.Data.DataHolder
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

// TODO: File saving does not work
// TODO: Refactor and check all the code

class Main : Application() {
    override fun start(stage: Stage) {
        DataHolder.primaryStage = stage
        DataHolder.services = hostServices // Used to openFile a hyperlink via the default browser in about dialog

        loadIcons()

        val loader = FXMLLoader(javaClass.getResource("Views/MainView.fxml"))
        val root = loader.load<Parent>()
        val css = javaClass.getResource("Views/Main.css").toExternalForm()
        root.stylesheets.add(css)

        DataHolder.primaryStage.scene = Scene(root, 1280.0, 800.0)
        DataHolder.primaryStage.minWidth = 1280.0
        DataHolder.primaryStage.minHeight = 800.0
        DataHolder.primaryStage.show()
    }

    /**
     * Loads icons of all sizes, JavaFX automatically selects the correct icon for the OS
     */
    private fun loadIcons() {
        DataHolder.primaryStage.icons.addAll(
                Image(javaClass.getResource("Icons/16x16.png").toURI().toString(), 16.0, 16.0, true, true),
                Image(javaClass.getResource("Icons/20x20.png").toURI().toString(), 20.0, 20.0, true, true),
                Image(javaClass.getResource("Icons/24x24.png").toURI().toString(), 24.0, 24.0, true, true),
                Image(javaClass.getResource("Icons/32x32.png").toURI().toString(), 32.0, 32.0, true, true),
                Image(javaClass.getResource("Icons/48x48.png").toURI().toString(), 48.0, 48.0, true, true),
                Image(javaClass.getResource("Icons/64x64.png").toURI().toString(), 64.0, 64.0, true, true),
                Image(javaClass.getResource("Icons/128x128.png").toURI().toString(), 128.0, 128.0, true, true),
                Image(javaClass.getResource("Icons/256x256.png").toURI().toString(), 256.0, 256.0, true, true),
                Image(javaClass.getResource("Icons/512x512.png").toURI().toString(), 512.0, 512.0, true, true)
        )
    }
}

fun main(args: Array<String>) {
    Application.launch(Main::class.java, *args)
}
