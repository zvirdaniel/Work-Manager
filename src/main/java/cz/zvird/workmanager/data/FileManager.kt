package cz.zvird.workmanager.data

import java.io.File
import java.time.Year
import java.util.prefs.Preferences

/**
 * Manages currently opened file
 */
object FileManager {
	private const val LAST_USED_FILE = "last_used_file"
	private const val PATH_NOT_FOUND = "path_not_found"
	private var currentFile: File? = null

	/**
	 * Loads the file to memory and displays it
	 * @throws Exception if file is not valid
	 */
	fun load(file: File) {
		val lastFile = currentFile
		currentFile = file

		try {
			MemoryManager.loadDataFromCurrentFile()
			Preferences.userNodeForPackage(FileManager::class.java).put(LAST_USED_FILE, file.absolutePath)
		} catch (e: Exception) {
			currentFile = lastFile
			throw e
		}
	}

	/**
	 * @return currently opened file (if one is opened), or last used file (if exists) or a temporary blank file
	 */
	fun retrieve(): File {
		val file = currentFile
		if (file != null) {
			return file
		}

		val lastUsedPath = Preferences.userNodeForPackage(FileManager::class.java)[LAST_USED_FILE, PATH_NOT_FOUND]
		if (lastUsedPath != PATH_NOT_FOUND) {
			val lastUsedFile = File(lastUsedPath)

			if (lastUsedFile.exists() && lastUsedFile.isFile) {
				currentFile = lastUsedFile
				return retrieve()
			}
		}

		currentFile = new()
		return retrieve()
	}

	/**
	 * Saves all data into the selected file
	 * @param target implicitly current file
	 * @throws java.io.IOException
	 * @throws com.fasterxml.jackson.core.JsonGenerationException
	 * @throws com.fasterxml.jackson.databind.JsonMappingException
	 */
	fun save(target: File? = null) {
		if (target != null) {
			MemoryManager.saveDataToFile(target)
			load(target)
			return
		}

		MemoryManager.saveDataToFile()
	}

	/**
	 * Creates a file in the filesystem, writes blank data into it, and returns it
	 * @param target implicitly a temporary one
	 * @param year implicitly set to the current year
	 * @throws java.io.IOException if creating a blank file failed
	 * @returns created file
	 */
	fun new(target: File? = null, year: Int = Year.now(DataHolder.zone).value): File {
		val file: File = target ?: File.createTempFile("TemporaryWorkYear", ".json")
		MemoryManager.saveBlankFile(file, year)
		return file
	}
}