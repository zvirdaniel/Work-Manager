package cz.zvird.workmanager.data

import java.io.File
import java.util.prefs.Preferences

// TODO: Repair data without saving them, dialog with year inconsistency
// TODO: File validation

/**
 * Manages currently opened file
 */
object DataFile {
	private const val LAST_USED_FILE = "last_used_file"
	private const val FILE_NOT_EXISTS = "file_not_exists"
	private var currentFile: File? = null

	/**
	 * Loads the file to the memory and displays it
	 * @throws Exception if file is not valid
	 */
	fun load(file: File) {
		currentFile = file
		Preferences.userNodeForPackage(DataFile::class.java).put(LAST_USED_FILE, file.absolutePath)
		MemoryData.reloadCurrentFile()
		println("Working with file: $file")
		DataHolder.primaryStage.title = "WorkManager - ${MemoryData.currentYear} - ${file.name}"
	}

	/**
	 * @return currently opened file (if one is opened), or last used file (if exists) or temporary blank file
	 */
	fun retrieve(): File {
		val file = currentFile

		if (file != null) {
			return file
		}

		val lastUsedPath = Preferences.userNodeForPackage(DataFile::class.java)[LAST_USED_FILE, FILE_NOT_EXISTS]
		if (lastUsedPath == FILE_NOT_EXISTS) { // If there is no last used file, create temporary one
			new()
			return retrieve()
		}

		val lastUsedFile = File(lastUsedPath)
		return try {
			load(lastUsedFile)
			retrieve()
		} catch (e: Exception) {
			new()
			retrieve()
		}
	}

	/**
	 * Saves all the data into the selected file, implicitly current file
	 * @param target selected from the UI
	 * @throws java.io.IOException if creating blank file failed
	 */
	fun save(target: File? = null) {
		if (target != null) {
			MemoryData.saveDataToFile(target)
			load(target)
		}

		MemoryData.saveDataToFile()
	}

	/**
	 * Creates a file in the filesystem, implicitly a temporary one, writes blank data into it, and loads it as current
	 * @param target to write blank data into
	 * @throws java.io.IOException if creating blank file failed
	 */
	fun new(target: File? = null) {
		val file = if (target != null) target else File.createTempFile("TemporaryWorkYear", ".json")

		file?.let { MemoryData.saveBlankFile(it) }
		file?.let { load(it) }
	}
}