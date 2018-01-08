package cz.zvird.workmanager.data

import java.io.File
import java.time.Year
import java.util.prefs.Preferences

/**
 * Manages currently opened file
 */
object FileManager {
	private const val LAST_USED_FILE = "last_used_file"
	private const val FILE_NOT_EXISTS = "file_not_exists"
	private var currentFile: File? = null

	/**
	 * Loads the file to the memory and displays it
	 * @throws Exception if file is not valid
	 */
	fun load(file: File, validate: Boolean = false) {
		val lastFile = currentFile
		currentFile = file

		try {
			MemoryManager.fileRefresh(validate)
			Preferences.userNodeForPackage(FileManager::class.java).put(LAST_USED_FILE, file.absolutePath)
		} catch (e: Exception) {
			currentFile = lastFile
			throw e
		}

	}

	/**
	 * @return currently opened file (if one is opened), or last used file (if exists) or temporary blank file
	 */
	fun retrieve(): File {
		val file = currentFile

		if (file != null) {
			return file
		}

		val lastUsedPath = Preferences.userNodeForPackage(FileManager::class.java)[LAST_USED_FILE, FILE_NOT_EXISTS]
		if (lastUsedPath == FILE_NOT_EXISTS) { // If there is no last used file, create temporary one
			currentFile = new()
			return retrieve()
		}

		// Return last used file
		currentFile = File(lastUsedPath)
		return retrieve()
	}

	/**
	 * Saves all the data into the selected file, implicitly current file
	 * @param target selected from the UI
	 * @throws java.io.IOException if creating blank file failed
	 */
	fun save(target: File? = null) {
		if (target != null) {
			MemoryManager.saveDataToFile(target)
			load(target)
		}

		MemoryManager.saveDataToFile()
	}

	/**
	 * Creates a file in the filesystem, implicitly a temporary one, writes blank data into it, and returns it
	 * @param target to write blank data into
	 * @param year implicitly set to the current year
	 * @throws java.io.IOException if creating blank file failed
	 * @returns created file
	 */
	fun new(target: File? = null, year: Int = Year.now(DataHolder.zone).value): File {
		val file: File = target ?: File.createTempFile("TemporaryWorkYear", ".json")
		MemoryManager.saveBlankFile(file, year)
		return file
	}
}