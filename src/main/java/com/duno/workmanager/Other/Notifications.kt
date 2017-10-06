package com.duno.workmanager.Other

import javafx.util.Duration
import org.controlsfx.control.Notifications

/**
 * Created by Daniel Zvir on 6.10.17.
 */

/**
 * @param fileName that will show up in the notification
 * Creates a notification informing the user that the fileName cant be saved
 */
fun notifyCantSave(fileName: String) {
    Notifications.create()
            .title("WorkManager")
            .text("Soubor nelze uložit jako $fileName.")
            .hideAfter(Duration(4000.0))
            .showError()
}

/**
 * @param fileName that will show up in the notification
 * Creates a notification informing the user about successful saving of the given file
 */
fun notifySavedAs(fileName: String) {
    Notifications.create()
            .title("WorkManager")
            .text("Soubor uložen jako $fileName.")
            .hideAfter(Duration(4000.0))
            .showInformation()
}

/**
 * Shows error notification with a given message
 */
fun errorNotification(message: String) {
    Notifications.create()
            .title("WorkManager")
            .text(message)
            .hideAfter(Duration(4000.0))
            .showError()
}