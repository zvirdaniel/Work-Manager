package cz.zvird.workmanager.data

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import cz.zvird.workmanager.gui.informativeNotification
import cz.zvird.workmanager.gui.showYearSelectorDialog
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
			/**
			 * When using listeners on observable lists, they fire events only if something is added or removed within the lists themselves.
			 * The following lambda specifies which properties of T used in the list should be checked for changes as well. Therefore,
			 * when user changes something in that specific property, ChangeEvent is fired from the list itself and listeners can catch it.
			 */
			monthsInMemory[i] = FXCollections.observableArrayList<WorkSession> {
				arrayOf(it.durationProperty)
			}
		}
	}

	/**
	 * Reloads the JSON and displays it, automatically corrects the data structure if needed (without saving)
	 * @param validate will check and repair the data structure, shows dialogs to the user if needed
	 * @throws Exception if parsing or correcting the JSON fails irrecoverably
	 */
	fun reloadCurrentFile(validate: Boolean = false) {
		val file = DataFile.retrieve()
		val data: Pair<Int, HashMap<Int, List<WorkSessionRaw>>> = mapper.readValue(file) // Throws an exception if reading the file fails
		val monthsRaw = data.second
		var year = data.first

		if (validate) {
			if (year <= 0) {
				val newYear = showYearSelectorDialog(
						DataHolder.primaryStage.scene.window,
						"Korekce dat"
				)

				if (newYear == null || newYear <= 0) {
					informativeNotification("Špatně zadaný rok, použije se aktuální.")
					year = Year.now(DataHolder.zone).value
				} else {
					year = newYear
				}
			}
		}

		clearYearData()
		currentYear = year
		for (i in 1..12) {
			val monthFromFile = monthsRaw[i]?.map { WorkSession(it) }

			monthFromFile?.let {
				if (validate) {
					var changed = false

					it.filter { it.beginDateProperty.value.monthValue != i }.forEach {
						it.beginDateProperty.value = it.beginDateProperty.value.withMonth(i)
						changed = true
					}

					it.filter { it.beginDateProperty.value.year != currentYear }.forEach {
						it.beginDateProperty.value = it.beginDateProperty.value.withYear(currentYear)
						changed = true
					}

					val hourlyWage = it.firstOrNull()?.hourlyWageProperty?.value
					it.filter { it.hourlyWageProperty.value != hourlyWage }.forEach {
						it.hourlyWageProperty.value = hourlyWage
						changed = true
					}

					if (changed) informativeNotification("Datová struktura opravena")
				}

				monthsInMemory[i]?.addAll(it)
			}
		}

		DataHolder.primaryStage.title = "WorkManager - Rok ${MemoryData.currentYear} - ${file.name}"
	}

	/**
	 * Creates a blank JSON with basic data structure
	 * @param file to save the data into
	 * @param year used in the file
	 * @throws java.io.IOException
	 * @throws com.fasterxml.jackson.core.JsonGenerationException
	 * @throws com.fasterxml.jackson.databind.JsonMappingException
	 */
	fun saveBlankFile(file: File, year: Int) {
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