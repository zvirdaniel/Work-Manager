package com.duno.workmanager.Data

import com.duno.workmanager.Models.WorkYear
import java.io.File
import java.util.prefs.Preferences

/**
 * Created by Daniel Zvir on 6.10.17.
 */

/**
 * Manages currently opened file, see getPrimaryStage() and setPrimaryStage() methods
 */
object CurrentFile {
    private const val LAST_USED_FILE = "last_used_file"
    private const val FILE_NOT_EXISTS = "file_not_exists"
    private var currentFile: File? = null

    /**
     * @return true if file was setPrimaryStage properly, false if file was not valid
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
            DataHolder.primaryStage.title = "WorkManager - ${file.name}"
            return file
        }

        val lastUsedPath = Preferences.userNodeForPackage(CurrentFile::class.java)[LAST_USED_FILE, FILE_NOT_EXISTS]
        if (lastUsedPath == FILE_NOT_EXISTS) { // If there is no last used file, create temporary one
            createAndSetTempFile()
            return get()
        }

        val lastUsedFile = File(lastUsedPath)
        if (isValid(lastUsedFile)) { // If last used file is valid, setPrimaryStage it as current
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