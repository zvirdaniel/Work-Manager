package com.duno.workmanager.Data

import com.duno.workmanager.Controllers.MonthController
import com.duno.workmanager.Models.ObservableSession
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.io.File
import java.util.prefs.Preferences

/**
 * Created by Daniel Zvir on 13.08.2017.
 */
object DataManagement {
    private var lastUsedFile: File
        get() {
            val path = Preferences.userNodeForPackage(this::class.java)["last_used_file", "file_not_found_error"]
            if (path != "file_not_found_error") {
                return File(path)
            }
            return File(System.getProperty("user.home"))
        }
        set(value) = Preferences.userNodeForPackage(this::class.java).put("last_used_file", value.absolutePath)

    private val observableMonths = hashMapOf<Int, ObservableList<ObservableSession>>()

    init {
        for (i in 1..12) this.observableMonths[i] = FXCollections.observableArrayList<ObservableSession>()
        setFile(lastUsedFile)
    }

    fun setFile(file: File) {
        val workYear: WorkYear
        try { // If JSON not valid, file wont be set
            workYear = WorkYear(file)
        } catch (e: Exception) {
            return
        }

        for (monthFromData in workYear.months) {
            val value = monthFromData.value
            val key = monthFromData.key
            observableMonths[key]?.clear()
            observableMonths[key]?.addAll(value.map(::ObservableSession))
        }

        lastUsedFile = file
    }

    private val monthControllers = mutableListOf<MonthController>()
    fun addTab(controller: MonthController) {
        monthControllers.add(controller)

        if (monthControllers.count() == 12) {
            monthControllers.forEachIndexed { i, c ->
                c.table.items = observableMonths[i + 1]
            }
        }
    }
}