package com.duno.workmanager

import com.duno.workmanager.Data.WorkYear
import com.duno.workmanager.Models.MonthModel
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import java.io.File

// TODO: Limit GUI description input to 90 characters

class Main : Application() {
    override fun start(stage: Stage) {
        val loader = FXMLLoader(javaClass.getResource("Views/MainView.fxml"))
        val root = loader.load<Parent>()

        stage.title = "Work Manager"
        stage.scene = Scene(root, 1280.0, 800.0)
        stage.minWidth = 1280.0
        stage.minHeight = 800.0
        stage.show()

        println("runnin'")
    }
}


fun getObservableSessions(month: Int): ObservableList<MonthModel> {
    val workYear = WorkYear(2017, File("/home/zvird/result.json"))
    val observableSessions: ObservableList<MonthModel> = FXCollections.observableArrayList()

    for (session in workYear.getMonth(month)) {
        observableSessions.add(MonthModel(session))
    }

    return observableSessions
}