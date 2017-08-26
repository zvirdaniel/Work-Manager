package com.duno.workmanager

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class Main : Application() {
    lateinit var primaryStage: Stage

    override fun start(stage: Stage) {
        val loader = FXMLLoader(javaClass.getResource("Views/MainView.fxml"))
        val root = loader.load<Parent>()

        primaryStage = stage
        primaryStage.title = "WorkManager"
        primaryStage.scene = Scene(root, 1280.0, 800.0)
        primaryStage.minWidth = 1280.0
        primaryStage.minHeight = 800.0
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(Main::class.java, *args)
}