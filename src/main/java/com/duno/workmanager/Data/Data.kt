package com.duno.workmanager.Data

import com.duno.workmanager.Controllers.MonthController
import com.duno.workmanager.Models.ObservableSession
import com.duno.workmanager.Other.exceptionDialog
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.io.File
import java.util.prefs.Preferences

/**
 * Manages data in tabs (visible to user)
 */
object VisibleData {
    val observableMonths = hashMapOf<Int, ObservableList<ObservableSession>>()
    private val monthControllers = mutableListOf<MonthController>()

    init {
        for (i in 1..12) observableMonths[i] = FXCollections.observableArrayList<ObservableSession>()
        reloadCurrentFile()
    }

    fun reloadCurrentFile() {
        val file = CurrentFile.get()
        val workYear = WorkYear(file)
        setAndShowCurrentWorkYear(workYear)
    }

    private fun setAndShowCurrentWorkYear(workYear: WorkYear) {
        for ((key, value) in workYear.months) {
            observableMonths[key]?.clear()
            observableMonths[key]?.addAll(value.map(::ObservableSession))
        }
    }

    fun addTab(controller: MonthController) {
        monthControllers.add(controller)

        if (monthControllers.count() == 12) {
            monthControllers.forEachIndexed { i, c ->
                c.table.items = observableMonths[i + 1]
            }
        }
    }
}

/**
 * Manages currently opened file, see get() and set() methods
 */
object CurrentFile {
    private const val LAST_USED_FILE = "last_used_file"
    private const val FILE_NOT_EXISTS = "file_not_exists"
    private var currentFile: File? = null

    /**
     * @param showError shows exception dialog if set to true and file is not valid
     * @return true if file was set properly, false if file was not valid
     */
    fun set(file: File, showError: Boolean = false): Boolean {
        val result: Boolean

        if (isValid(file, showError)) {
            currentFile = file
            Preferences.userNodeForPackage(CurrentFile::class.java).put(LAST_USED_FILE, file.absolutePath)
            result = true
        } else {
            result = false
        }

        VisibleData.reloadCurrentFile()
        return result
    }

    /**
     * @return Currently opened file (if one is opened), or last used file (if exists) or temporary blank file
     */
    fun get(): File {
        val file = currentFile

        if (file != null) {
            println("File $file was returned")
            return file
        }

        val lastUsedPath = Preferences.userNodeForPackage(CurrentFile::class.java)[LAST_USED_FILE, FILE_NOT_EXISTS]
        if (lastUsedPath == FILE_NOT_EXISTS) { // If there is no last used file, create temporary one
            createAndSetTempFile()
            return get()
        }

        val lastUsedFile = File(lastUsedPath)
        if (isValid(lastUsedFile)) { // If last used file is valid, set it as current
            set(lastUsedFile)
            return get()
        }

        createAndSetTempFile()
        return get()
    }

    private fun createAndSetTempFile() {
        val file = File.createTempFile("TemporaryWorkYear", ".json")
        WorkYear().writeYearInJson(file)
        set(file)
    }

    private fun isValid(file: File, showError: Boolean = false): Boolean {
        try {
            WorkYear(file)
        } catch (e: Exception) {
            if (showError) exceptionDialog(e, "This file is not valid!")
            return false
        }

        return true
    }
}

/**
 * File management backend for GUI, called from controllers
 */
object FileManagement {
    /**
     * @param file selected from GUI
     */
    fun new(file: File) {
        val workYear = WorkYear()
        workYear.writeYearInJson(file)
        CurrentFile.set(file)
    }

    /**
     * @param file selected from GUI
     * Shows exception dialog if file is not valid
     */
    fun open(file: File) {
        CurrentFile.set(file, true)
    }

    /**
     * @return true if file was successfully written; false otherwise
     */
    fun saveAs(file: File): Boolean {
        val writeStatus = writeWorkYear(file)
        CurrentFile.set(file)
        return writeStatus
    }

    /**
     * @return true if file was successfully written; false otherwise
     */
    fun save(): Boolean {
        val currentFile = CurrentFile.get()
        return writeWorkYear(currentFile)
    }

    private fun writeWorkYear(file: File): Boolean {
        val workYear = WorkYear()
        for (month in VisibleData.observableMonths) {
            workYear.addAllToMonth(month.key, month.value)
        }

        return workYear.writeYearInJson(file)
    }
}