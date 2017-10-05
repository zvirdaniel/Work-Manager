package com.duno.workmanager.Data

import com.duno.workmanager.Controllers.TableViewController
import com.duno.workmanager.Models.ObservableSession
import com.duno.workmanager.PrimaryStage
import javafx.application.HostServices
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.controlsfx.control.MaskerPane
import java.io.File
import java.util.prefs.Preferences

/**
 * Holds static variables
 */
object Holder {
    var services: HostServices? = null // Used in about dialog to open a link in a web browser
    val maskerPane = MaskerPane()
    private val tableViewControllers = mutableListOf<TableViewController>()

    init {
        maskerPane.visibleProperty().value = false
    }

    /**
     * @return controller instance of a given month
     */
    fun getTableViewController(index: Int): TableViewController {
        return tableViewControllers[index]
    }

    /**
     * @param controller instance to be added into the list, can be retrieved later
     */
    fun addTableViewController(controller: TableViewController) {
        tableViewControllers.add(controller)

        if (tableViewControllers.count() == 12) {
            tableViewControllers.forEachIndexed { i, c ->
                c.table.items = VisibleData.visibleDataMap[i + 1]
            }
        }
    }
}

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
            PrimaryStage.get().title = "WorkManager - ${file.name}"
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
     */
    fun save(): Boolean {
        val currentFile = CurrentFile.get()
        return writeCurrentWorkYear(currentFile)
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
     * @param file to write the data into, it will be overwritten
     * @return false if exception is thrown, true otherwise
     * Saves the data in memory into a given file as a JSON
     */
    private fun writeCurrentWorkYear(file: File): Boolean {
        val workYear = VisibleData.generateWorkYearFromVisibleData()
        return workYear.writeYearInJson(file)
    }
}

/**
 * Exports the visible data into a file as a spreadsheet
 * This has to be out of FileManagement, because it can't be referenced to a function in an object
 * @return true if file was successfully written; false otherwise
 * @param monthRange of months to export into file
 * @param file selected from GUI
 */
fun exportToSpreadsheet(monthRange: IntRange, file: File): Boolean {
    try {
        file.createNewFile()
    } catch (e: Exception) {
        return false
    }

    val workYear = VisibleData.generateWorkYearFromVisibleData()
    return workYear.writeYearInXlsx(file, monthRange)
}