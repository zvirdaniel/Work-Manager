package com.duno.workmanager.Data

import com.duno.workmanager.Models.ObservableSession
import com.duno.workmanager.Models.WorkYear
import javafx.collections.FXCollections
import javafx.collections.ObservableList

/**
 * Created by Daniel Zvir on 6.10.17.
 */

/**
 * Manages data in memory - in UI
 */
object VisibleData {
    val visibleDataMap = hashMapOf<Int, ObservableList<ObservableSession>>() // Holds the visible data of all months

    init {
        for (i in 1..12) visibleDataMap[i] = FXCollections.observableArrayList<ObservableSession>()
        reloadCurrentFile()
    }

    /**
     * Remaps data from the current file into visibleDataMap
     */
    fun reloadCurrentFile() {
        val file = CurrentFile.get()
        val workYear = WorkYear(file)
        setAndShowCurrentWorkYear(workYear)
    }

    /**
     * @return WorkYear containing the data from the UI
     * Creates a WorkYear from the data contained in the UI
     */
    fun generateWorkYearFromVisibleData(): WorkYear {
        val workYear = WorkYear()
        for ((key, value) in visibleDataMap) {
            workYear.addAllToMonth(key, value)
        }

        return workYear
    }

    /**
     * @param workYear used as data source for import, it's mapped into visibleDataMap
     * Imports data from workYear into visibleDataMap - into UI
     */
    private fun setAndShowCurrentWorkYear(workYear: WorkYear) {
        for ((key, value) in workYear.months) {
            visibleDataMap[key]?.clear()
            visibleDataMap[key]?.addAll(value.map(::ObservableSession))
        }
    }
}