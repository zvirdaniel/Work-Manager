package cz.zvird.workmanager.models

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.io.File

/**
 * Manages the data visible in the user interface, and handles the file interaction
 */
class Data(year: Int) {
	private val monthsInMemory = hashMapOf<Int, ObservableList<WorkSession>>() // contains all the data visible in the UI
	var currentYear = year
		set(value) {
			if (value < 0) {
				throw IllegalArgumentException("Year number must be bigger than zero!")
			}

			field = value
		}

	init {
		for (i in 1..12) {
			this.monthsInMemory[i] = FXCollections.observableArrayList<WorkSession>()
		}
	}

	constructor(file: File) : this(0) {
		loadFromFile(file)
	}

	/**
	 * Parses the JSON and loads it into the memory, does not check the data structure
	 * @param file to save the data into
	 * @throws Exception if parsing the JSON fails irrecoverably
	 */
	fun loadFromFile(file: File) {
		val mapper = jacksonObjectMapper()
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
	 * Generates a JSON containing a Pair with the current year, and a list with all months saved as WorkSessionRaw
	 * @param file to save the data into
	 * @throws java.io.IOException
	 * @throws com.fasterxml.jackson.core.JsonGenerationException
	 * @throws com.fasterxml.jackson.databind.JsonMappingException
	 */
	fun saveToFile(file: File) {
		val mapper = jacksonObjectMapper()

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
	fun getMonth(monthNumber: Int): MutableList<WorkSession> {
		val month = monthsInMemory[monthNumber]

		if (monthNumber in 1..12 && month != null) {
			return month
		} else {
			throw IllegalArgumentException("Month numbers can only be between 1 and 12!")
		}
	}
}