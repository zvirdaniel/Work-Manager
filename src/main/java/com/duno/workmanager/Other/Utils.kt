package com.duno.workmanager.Other

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import org.controlsfx.control.Notifications
import java.time.ZoneId
import java.util.*

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

fun isOnSameDay(first: Date, second: Date): Boolean {
    val localFirst = first.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val localSecond = second.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    return localFirst.equals(localSecond)
}