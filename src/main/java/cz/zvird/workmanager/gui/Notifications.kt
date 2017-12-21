package cz.zvird.workmanager.gui

import javafx.util.Duration
import org.controlsfx.control.Notifications

/**
 * Creates a notification informing the user that the fileName cant be saved
 * @param fileName that will show up in the notification
 */
fun cantSaveNotification(fileName: String) {
	Notifications.create()
			.title("WorkManager")
			.text("Soubor nelze uložit jako $fileName.")
			.hideAfter(Duration(4000.0))
			.showError()
}

/**
 * Creates a notification informing the user about successful saving of the given file
 * @param fileName that will show up in the notification
 */
fun savedAsNotification(fileName: String) {
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