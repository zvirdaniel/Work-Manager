package cz.zvird.workmanager.gui

import cz.zvird.workmanager.data.DataHolder
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import kotlin.concurrent.thread

/**
 * Executes body in the background thread, while blocking the keyboard input and the entire UI using the MaskerPane object
 * @param body function reference, will be executed in a different thread
 */
class BlockedTask(private val body: () -> Unit) {
	// Event consumer has to be stored in a variable, in order to be able to remove the reference
	private val consumer = EventHandler<KeyEvent> { it.consume() }

	/**
	 * Disables keyboard input entirely by consuming all key events
	 */
	private fun disableKeyboardShortcuts() {
		DataHolder.primaryStage.addEventFilter(KeyEvent.ANY, consumer)
	}

	/**
	 *  Enables keyboard input by removing the event consumer
	 */
	private fun enableKeyboardShortcuts() {
		DataHolder.primaryStage.removeEventFilter(KeyEvent.ANY, consumer)
	}

	private val task = object : Task<Unit>() {
		override fun done() {
			DataHolder.maskerPane.progressProperty().unbind()
			DataHolder.maskerPane.textProperty().unbind()
			DataHolder.maskerPane.visibleProperty().unbind()
			DataHolder.maskerPane.visibleProperty().value = false
			enableKeyboardShortcuts()
		}

		override fun call() {
			updateMessage("Probíhá zpracování")

			body()

			updateMessage("Zpracování dokončeno")
			updateProgress(100, 100)
			Thread.sleep(500)

			return
		}
	}

	init {
		if (!Platform.isFxApplicationThread()) {
			throw IllegalThreadStateException("BlockedTask has to run on the JavaFX thread!")
		}

		disableKeyboardShortcuts()
		DataHolder.maskerPane.progressProperty().bind(task.progressProperty())
		DataHolder.maskerPane.textProperty().bind(task.messageProperty())
		DataHolder.maskerPane.visibleProperty().bind(task.runningProperty())

		thread { task.run() }
	}
}