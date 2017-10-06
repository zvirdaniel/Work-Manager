package com.duno.workmanager.Other

import com.duno.workmanager.Data.DataHolder
import javafx.concurrent.Task

object Utilities {
    /**
     * Executes body while blocking UI using MaskerPane
     * @param body function reference
     */
    open class BlockedTask<T>(private val body: () -> T) : Task<T>() {
        init {
            DataHolder.maskerPane.progressProperty().bind(this.progressProperty())
            DataHolder.maskerPane.textProperty().bind(this.messageProperty())
            DataHolder.maskerPane.visibleProperty().bind(this.runningProperty())
        }

        override fun call(): T {
            updateMessage("Probíhá zpracování")

            val result = body()

            Thread.sleep(1000)
            updateMessage("Zpracování dokončeno")
            updateProgress(100, 100)

            return result
        }
    }
}