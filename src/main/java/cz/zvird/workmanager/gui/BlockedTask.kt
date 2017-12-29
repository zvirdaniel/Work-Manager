package cz.zvird.workmanager.gui

import cz.zvird.workmanager.data.DataHolder
import javafx.concurrent.Task

/**
 * Executes body while blocking the entire UI using MaskerPane
 * @param body function reference
 */
open class BlockedTask(private val body: () -> Unit) : Task<Unit>() {
	init {
		DataHolder.maskerPane.progressProperty().bind(this.progressProperty())
		DataHolder.maskerPane.textProperty().bind(this.messageProperty())
		DataHolder.maskerPane.visibleProperty().bind(this.runningProperty())
	}

	override fun done() {
		DataHolder.maskerPane.progressProperty().unbind()
		DataHolder.maskerPane.textProperty().unbind()
		DataHolder.maskerPane.visibleProperty().unbind()
		DataHolder.maskerPane.visibleProperty().value = false
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