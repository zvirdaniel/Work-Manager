package cz.zvird.workmanager.gui

import cz.zvird.workmanager.data.DataHolder
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.scene.input.KeyEvent

/**
 * Executes body while blocking keyboard input, and the entire UI using MaskerPane
 * @param body function reference
 */
open class BlockedTask(private val body: () -> Unit) : Task<Unit>() {
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

	init {
		disableKeyboardShortcuts()
		DataHolder.maskerPane.progressProperty().bind(this.progressProperty())
		DataHolder.maskerPane.textProperty().bind(this.messageProperty())
		DataHolder.maskerPane.visibleProperty().bind(this.runningProperty())
	}

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

		Thread.sleep(1000)
		updateMessage("Zpracování dokončeno")
		updateProgress(100, 100)

		return
	}
}