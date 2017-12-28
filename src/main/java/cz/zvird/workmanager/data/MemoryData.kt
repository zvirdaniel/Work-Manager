package cz.zvird.workmanager.data

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import cz.zvird.workmanager.models.WorkSession
import cz.zvird.workmanager.models.WorkSessionRaw
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.io.File
import java.time.Year

/**
 * Manages the data visible in the user interface, and handles the file interaction
 */
object MemoryData {
	private val mapper = jacksonObjectMapper()
	private val monthsInMemory = hashMapOf<Int, ObservableList<WorkSession>>() // contains all the data visible in the UI
	var currentYear = 0
		set(value) {
			if (value < 0) {
				throw IllegalArgumentException("Year number must be bigger than zero!")
			}

			field = value
		}

	init {
		for (i in 1..12) {
			monthsInMemory[i] = FXCollections.observableArrayList<WorkSession>()
		}

		reloadCurrentFile()
	}

	/**
	 * Reloads the JSON and displays it
	 * @throws Exception if parsing the JSON fails irrecoverably
	 */
	fun reloadCurrentFile() {
		val file = DataFile.retrieve()
		val data: Pair<Int, HashMap<Int, List<WorkSessionRaw>>> = mapper.readValue(file)
		val monthsRaw = data.second
		currentYear = data.first

		clearYearData()
		for (i in 1..12) {
			val monthFromFile = monthsRaw[i]?.map { WorkSession(it) }

			monthFromFile?.let {
				monthsInMemory[i]?.addAll(it)
			}
		}
	}

	/**
	 * Creates a blank JSON with basic data structure
	 * @param file to save the data into
	 * @param year implicitly set to the current year
	 * @throws java.io.IOException
	 * @throws com.fasterxml.jackson.core.JsonGenerationException
	 * @throws com.fasterxml.jackson.databind.JsonMappingException
	 */
	fun saveBlankFile(file: File, year: Int = Year.now().value) {
		val monthsRaw = hashMapOf<Int, List<WorkSessionRaw>>()
		for (i in 1..12) {
			monthsRaw[i] = listOf()
		}

		val data = Pair(year, monthsRaw)
		mapper.writeValue(file, data)
	}

	/**
	 * Creates a JSON containing a Pair with the current year, and a list with all months saved as WorkSessionRaw
	 * @param target to save data into, implicitly current file
	 * @throws java.io.IOException
	 * @throws com.fasterxml.jackson.core.JsonGenerationException
	 * @throws com.fasterxml.jackson.databind.JsonMappingException
	 */
	fun saveDataToFile(target: File? = null) {
		val file: File = if (target != null) target else DataFile.retrieve()

		val monthsRaw = hashMapOf<Int, List<WorkSessionRaw>>()
		for (i in 1..12) {
			val list = mutableListOf<WorkSessionRaw>()
			getMonth(i).forEach { list.add(it.getRawData()) }
			monthsRaw[i] = list.toList()
		}

		val data = Pair(currentYear, monthsRaw)
		mapper.writeValue(file, data)
	}

	/**
	 * Deletes all data from each month
	 */
	fun clearYearData() {
		for (month in monthsInMemory) {
			month.value.clear()
		}
	}

	/**
	 * @return list with WorkSessions for a given month
	 * @param monthNumber between 1 and 12
	 */
	fun getMonth(monthNumber: Int): ObservableList<WorkSession> {
		val month = monthsInMemory[monthNumber]

		if (monthNumber in 1..12 && month != null) {
			return month
		} else {
			throw IllegalArgumentException("Month numbers can only be between 1 and 12!")
		}
	}
}