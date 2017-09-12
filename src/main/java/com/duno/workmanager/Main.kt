package com.duno.workmanager

import com.duno.workmanager.Other.exportDialog
import com.duno.workmanager.Other.services
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

// TODO: progress bar, use MaskerPane
// TODO: Dialog owner
// TODO: Notify the user that temporary file was created
// TODO: Complete documentation

class Main : Application() {
    override fun start(stage: Stage) {
        PrimaryStage.set(stage)

        services = hostServices

        loadIcons()

        val loader = FXMLLoader(javaClass.getResource("Views/MainView.fxml"))
        val root = loader.load<Parent>()
        val css = Main::class.java.getResource("Views/Main.css").toExternalForm()
        root.stylesheets.add(css)

        PrimaryStage.get().title = "WorkManager"
        PrimaryStage.get().scene = Scene(root, 1280.0, 800.0)
        PrimaryStage.get().minWidth = 1280.0
        PrimaryStage.get().minHeight = 800.0
        PrimaryStage.get().show()

        exportDialog()
    }

    private fun loadIcons() {
        PrimaryStage.get().icons.addAll(
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

object PrimaryStage {
    private var stage: Stage? = null

    fun get(): Stage {
        val s = stage
        if (s != null) {
            return s
        } else {
            throw Exception("Stage not initialized!")
        }
    }

    fun set(s: Stage) {
        stage = s
    }
}

fun main(args: Array<String>) {
    Application.launch(Main::class.java, *args)
}