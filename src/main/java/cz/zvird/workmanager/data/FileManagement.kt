package cz.zvird.workmanager.data

import cz.zvird.workmanager.models.WorkYear
import cz.zvird.workmanager.models.writeYearInXlsx
import java.io.File

/**
 * Creates a new file and writes blank WorkYear into it
 * @param file selected from GUI
 * @throws java.io.IOException if creating blank file failed
 */
fun newFile(file: File) {
	file.createNewFile()

	val workYear = WorkYear()
	workYear.writeYearInJson(file)
	CurrentFile.set(file)
}

/**
 * @throws Exception if file is not valid
 * @param file selected from GUI
 */
fun openFile(file: File) {
	CurrentFile.set(file)
}

/**
 * Saves the visible data into currently opened file
 * @throws java.io.IOException if creating blank file failed
 */
fun saveFile() {
	val currentFile = CurrentFile.get()
	writeCurrentWorkYear(currentFile)
}

/**
 * Saves the visible data into selected file
 * @param file selected from GUI
 * @throws java.io.IOException if creating blank file failed
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
 * Saves the visible data into a given file as a JSON
 * @param file to write the data into, it will be overwritten
 */
private fun writeCurrentWorkYear(file: File) {
	val workYear = VisibleData.generateWorkYearFromVisibleData()
	workYear.writeYearInJson(file)
}