package cz.zvird.workmanager.data

import cz.zvird.workmanager.models.WorkYear
import cz.zvird.workmanager.models.writeYearInXlsx
import java.io.File

/**
 * @param file selected from GUI
 * Creates a newFile file and writes blank WorkYear into it
 */
fun newFile(file: File) {
    file.createNewFile()

    val workYear = WorkYear()
    workYear.writeYearInJson(file)
	CurrentFile.set(file)
}

/**
 * @param file selected from GUI
 */
fun openFile(file: File) {
	CurrentFile.set(file)
}

/**
 * Saves current file
 */
fun saveFile() {
    val currentFile = CurrentFile.get()
	writeCurrentWorkYear(currentFile)
}

/**
 * @param file selected from GUI
 */
fun saveFileAs(file: File) {
    file.createNewFile()
	writeCurrentWorkYear(file)
	CurrentFile.set(file)
}

/**
 * Exports the visible data into a file as a spreadsheet
 * @param monthRange of months to export into file
 * @param file selected from GUI
 */
fun exportToSpreadsheet(monthRange: IntRange, file: File) {
    file.createNewFile()
    val workYear = VisibleData.generateWorkYearFromVisibleData()
    workYear.writeYearInXlsx(file, monthRange)
}

/**
 * @param file to write the data into, it will be overwritten
 * Saves the data in memory into a given file as a JSON
 */
private fun writeCurrentWorkYear(file: File) {
    val workYear = VisibleData.generateWorkYearFromVisibleData()
    workYear.writeYearInJson(file)
}