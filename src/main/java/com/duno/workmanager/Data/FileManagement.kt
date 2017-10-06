package com.duno.workmanager.Data

import com.duno.workmanager.Models.WorkYear
import com.duno.workmanager.Models.writeYearInXlsx
import java.io.File

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
     * @return true if file is valid and was setPrimaryStage, false otherwise
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