package com.duno.workmanager.Data

import com.duno.workmanager.Controllers.MonthController
import com.duno.workmanager.Models.ObservableSession
import com.duno.workmanager.PrimaryStage
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.io.File
import java.util.prefs.Preferences

/**
 * Manages data in memory (visible to user)
 */
object VisibleData {
    val observableMonths = hashMapOf<Int, ObservableList<ObservableSession>>()
    private val monthControllers = mutableListOf<MonthController>()

    init {
        for (i in 1..12) observableMonths[i] = FXCollections.observableArrayList<ObservableSession>()
        reloadCurrentFile()
    }

    /**
     * Remaps data from the current file into observableMonths
     */
    fun reloadCurrentFile() {
        val file = CurrentFile.get()
        val workYear = WorkYear(file)
        setAndShowCurrentWorkYear(workYear)
    }

    /**
     * @param workYear used as data source for import, it's mapped into observableMonths
     * Imports data from workYear into observableMonths, which are visible to user
     */
    private fun setAndShowCurrentWorkYear(workYear: WorkYear) {
        for ((key, value) in workYear.months) {
            observableMonths[key]?.clear()
            observableMonths[key]?.addAll(value.map(::ObservableSession))
        }
    }

    /**
     * @return MonthController instance for a given month
     */
    fun getTabController(index: Int): MonthController {
        return monthControllers[index]
    }

    /**
     * @param controller instance to be added into the list, can be retrieved later
     */
    fun addTabController(controller: MonthController) {
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
     * @return true if file was set properly, false if file was not valid
     */
    fun set(file: File): Boolean {
        val status: Boolean

        if (isValid(file)) {
            currentFile = file
            Preferences.userNodeForPackage(CurrentFile::class.java).put(LAST_USED_FILE, file.absolutePath)
            PrimaryStage.get().title = "WorkManager - ${file.name}"
            status = true
        } else {
            status = false
        }

        VisibleData.reloadCurrentFile()
        return status
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

    /**
     * Creates a temporary file in the filesystem and sets it as current
     */
    private fun createAndSetTempFile() {
        val file = File.createTempFile("TemporaryWorkYear", ".json")
        WorkYear().writeYearInJson(file)
        set(file)
    }

    /**
     * @return true if file was parsed as a WorkYear, false if exception was thrown
     * @param file to open
     */
    private fun isValid(file: File): Boolean {
        try {
            WorkYear(file)
        } catch (e: Exception) {
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
     * @return true if file successfully written, false otherwise
     * Creates a new file and writes blank WorkYear into it
     */
    fun new(file: File): Boolean {
        try {
            file.createNewFile()
        } catch (e: Exception) {
            return false
        }

        val workYear = WorkYear()
        val writeStatus = workYear.writeYearInJson(file)
        CurrentFile.set(file)
        return writeStatus
    }

    /**
     * @param file selected from GUI
     * @return true if file is valid and was set, false otherwise
     */
    fun open(file: File): Boolean {
        return CurrentFile.set(file)
    }

    /**
     * @return true if file was successfully written; false otherwise
     * @param file selected from GUI
     */
    fun saveAs(file: File): Boolean {
        try {
            file.createNewFile()
        } catch (e: Exception) {
            return false
        }

        val writeStatus = writeCurrentWorkYear(file)
        CurrentFile.set(file)
        return writeStatus
    }

    /**
     * @return true if file was successfully written; false otherwise
     */
    fun save(): Boolean {
        val currentFile = CurrentFile.get()
        return writeCurrentWorkYear(currentFile)
    }

    /**
     * @param file to write the data into, it will be overwritten
     * @return false if exception is thrown, true otherwise
     * Saves the data in memory into into a given file as a WorkYear
     */
    private fun writeCurrentWorkYear(file: File): Boolean {
        val workYear = WorkYear()
        for (month in VisibleData.observableMonths) {
            workYear.addAllToMonth(month.key, month.value)
        }

        return workYear.writeYearInJson(file)
    }
}