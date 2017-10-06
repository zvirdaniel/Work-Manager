package com.duno.workmanager.Other

import javafx.concurrent.Task
import java.time.ZoneId
import java.util.*

/**
 * @param body function reference
 */
open class ProgressTask<T>(private val body: () -> T) : Task<T>() {
    override fun call(): T {
        updateMessage("Probíhá zpracování")

        val result = body()

        Thread.sleep(1000)
        updateMessage("Zpracování dokončeno")
        updateProgress(100, 100)

        return result
    }
}

/**
 * @return true if dates are on the same day, does not compare time
 */
fun isOnSameDay(first: Date, second: Date): Boolean {
    val localFirst = first.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val localSecond = second.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    return localFirst == localSecond
}