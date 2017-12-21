package cz.zvird.workmanager.data

import cz.zvird.workmanager.models.WorkYear
import java.io.File
import java.util.prefs.Preferences

// TODO: Repair data without saving them, dialog with year inconsistency

/**
 * Manages currently opened file, see set and get methods
 */
object CurrentFile {
	private const val LAST_USED_FILE = "last_used_file"
	private const val FILE_NOT_EXISTS = "file_not_exists"
	private var currentFile: File? = null

	/**
	 * @throws Exception if file is not valid
	 */
	fun set(file: File) {
		validate(file)
		currentFile = file
		Preferences.userNodeForPackage(CurrentFile::class.java).put(LAST_USED_FILE, file.absolutePath)
		VisibleData.reloadCurrentFile()
	}

	/**
	 * @return currently opened file (if one is opened), or last used file (if exists) or temporary blank file
	 */
	fun get(): File {
		val file = currentFile

		if (file != null) {
			println("Working with file: $file")
			DataHolder.primaryStage.title = "WorkManager - ${file.name}"
			return file
		}

		val lastUsedPath = Preferences.userNodeForPackage(CurrentFile::class.java)[LAST_USED_FILE, FILE_NOT_EXISTS]
		if (lastUsedPath == FILE_NOT_EXISTS) { // If there is no last used file, create temporary one
			createAndSetTempFile()
			return get()
		}

		val lastUsedFile = File(lastUsedPath)
		return try {
			set(lastUsedFile)
			get()
		} catch (e: Exception) {
			createAndSetTempFile()
			get()
		}
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
	 * Attempts parsing the json into WorkYear, does not check the data structure
	 * @param file to open
	 * @throws Exception if file can not be parsed
	 */
	private fun validate(file: File) {
		val workYear = WorkYear(file)
	}
}