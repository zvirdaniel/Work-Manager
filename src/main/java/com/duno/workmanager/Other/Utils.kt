package com.duno.workmanager.Other

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import org.controlsfx.control.Notifications
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Created by Daniel Zvir on 16.08.2017.
 */
fun exceptionDialog(e: Exception, message: String = "") {
    val alert = Alert(Alert.AlertType.ERROR)
    alert.title = "Exception"
    alert.headerText = "Look! An exception!"
    alert.contentText = message
    alert.width = 500.0

    val sw = StringWriter()
    val pw = PrintWriter(sw)
    e.printStackTrace(pw)
    val exception = sw.toString()
    val label = Label("The exception stacktrace was: ")
    val textArea = TextArea(exception)

    textArea.isEditable = false
    textArea.isWrapText = true
    textArea.prefWidth = 800.0
    textArea.prefHeight = 400.0
    GridPane.setVgrow(textArea, Priority.ALWAYS)
    GridPane.setHgrow(textArea, Priority.ALWAYS)

    val expContent = GridPane()
    expContent.add(label, 0, 0)
    expContent.add(textArea, 0, 1)

    alert.dialogPane.expandableContent = expContent
    alert.showAndWait()
}

fun errorDialog(message: String) {
    val alert = Alert(AlertType.ERROR)
    alert.title = "Error"
    alert.headerText = "Look! An error"
    alert.contentText = message
    alert.showAndWait()
}

fun errorNotification(message: String) {
    Notifications.create()
            .title("WorkManager")
            .text(message)
            .hideAfter(javafx.util.Duration(4000.0))
            .showError()
}