package cz.zvird.workmanager.data

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import cz.zvird.workmanager.gui.informativeNotification
import cz.zvird.workmanager.gui.showYearSelectorDialog
import cz.zvird.workmanager.models.WorkMonth
import cz.zvird.workmanager.models.WorkSession
import cz.zvird.workmanager.models.WorkSessionRaw
import cz.zvird.workmanager.models.WorkYear
import cz.zvird.workmanager.safeCall
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.io.File
import java.time.Year

/**
 * Manages the data in memory, and handles file interaction
 */
object MemoryManager {
	internal val workYear: WorkYear

	init {
		val months = hashMapOf<Int, WorkMonth>()
		for (i in 1..12) {
			/**
			 * When using listeners on observable lists, they fire events only if something is added or removed within the lists.
			 * The following lambda specifies which properties of T used in the list should be checked for changes as well. Therefore,
			 * when user changes something in that specific property, ChangeEvent is fired from the list itself and listeners can catch it.
			 */
			months[i] = WorkMonth(
					FXCollections.observableArrayList<WorkSession> {
						arrayOf(it.beginDateProperty, it.beginTimeProperty, it.durationProperty, it.descriptionProperty)
					}, 0
			)
		}

		workYear = WorkYear(Year.now().value, months)
	}

	var currentYear
		set(value) {
			if (value < 0) {
				throw IllegalArgumentException("Year number must be bigger than zero!")
			}

			workYear.year = value
		}
		get() = workYear.year

	/**
	 * Reloads the JSON and displays it, corrects the data structure if requested
	 * @param validate will check and repair the data structure, shows dialogs to the user if needed
	 * @throws Exception if parsing or correcting the JSON fails irrecoverably
	 */
	fun fileRefresh(validate: Boolean = false) {
		val file = FileManager.retrieve()
		val rawDataFromFile: Pair<Int, HashMap<Int, Pair<List<WorkSessionRaw>, Int>>> = jacksonObjectMapper().readValue(file)

		val rawMonthsFromFile: HashMap<Int, Pair<List<WorkSessionRaw>, Int>> = rawDataFromFile.second
		val workMonthsFromFile: HashMap<Int, WorkMonth> = hashMapOf()
		for (i in 1..12) {
			val hourlyWage = rawMonthsFromFile[i]?.second ?: 0
			val observableMonth: ObservableList<WorkSession> = FXCollections.observableArrayList(rawMonthsFromFile[i]?.first?.map { WorkSession(it) })
			workMonthsFromFile[i] = WorkMonth(observableMonth, hourlyWage)
		}

		val workYearFromFile = WorkYear(rawDataFromFile.first, workMonthsFromFile)

		if (validate) validateAndRepair(workYearFromFile)

		workYear.year = workYearFromFile.year
		for (i in 1..12) {
			val monthInMemory = workYear.months[i]
			monthInMemory?.sessions?.clear()
			workYearFromFile.months[i]?.sessions?.forEach { monthInMemory?.sessions?.add(it) }
			monthInMemory?.hourlyWage = workYearFromFile.months[i]?.hourlyWage ?: 0
		}

		safeCall {
			DataHolder.mainController.refreshBottomBarUI()
			DataHolder.primaryStage.title = "${DataHolder.appTitle} - Rok ${MemoryManager.currentYear} - ${file.name}"
			DataHolder.mainController.sortTableAndFocus()
		}
	}

	/**
	 * Checks the data structure for data consistency, and repairs it if needed
	 * @param yearToValidate instance to check and repair
	 */
	private fun validateAndRepair(yearToValidate: WorkYear) {
		if (yearToValidate.year <= 0) {
			val newYear = showYearSelectorDialog(
					DataHolder.primaryStage.scene.window, "Korekce dat"
			)?.toIntOrNull()

			if (newYear == null || newYear <= 0) {
				informativeNotification("Špatně zadaný rok, použije se aktuální.")
				yearToValidate.year = Year.now(DataHolder.zone).value
			} else {
				yearToValidate.year = newYear
			}
		}

		var hasChanged = false
		for (i in 1..12) {
			yearToValidate.months[i]?.sessions?.let {
				it.filter { it.beginDateProperty.value.monthValue != i }.forEach {
					it.beginDateProperty.value = it.beginDateProperty.value.withMonth(i)
					hasChanged = true
				}

				it.filter { it.beginDateProperty.value.year != yearToValidate.year }.forEach {
					it.beginDateProperty.value = it.beginDateProperty.value.withYear(yearToValidate.year)
					hasChanged = true
				}

				it.filter { it.durationProperty.value.isNegative }.forEach {
					it.durationProperty.value = it.durationProperty.value.negated()
					hasChanged = true
				}
			}
		}

		if (hasChanged) {
			informativeNotification("Datová struktura opravena.")
		}
	}

	/**
	 * Creates a blank JSON with basic data structure
	 * @param file to save the data into
	 * @param year used in the file
	 * @throws java.io.IOException
	 * @throws com.fasterxml.jackson.core.JsonGenerationException
	 * @throws com.fasterxml.jackson.databind.JsonMappingException
	 */
	fun saveBlankFile(file: File, year: Int = Year.now().value) {
		val months: HashMap<Int, Pair<List<WorkSessionRaw>, Int>> = hashMapOf()

		for (i in 1..12) {
			months[i] = Pair(listOf(), 0)
		}

		val data: Pair<Int, HashMap<Int, Pair<List<WorkSessionRaw>, Int>>> = Pair(year, months)
		jacksonObjectMapper().writeValue(file, data)
	}

	/**
	 * Creates a JSON containing the user data
	 * @param target to save data into, implicitly currently opened one
	 * @throws java.io.IOException
	 * @throws com.fasterxml.jackson.core.JsonGenerationException
	 * @throws com.fasterxml.jackson.databind.JsonMappingException
	 */
	fun saveDataToFile(target: File? = null) {
		val file: File = target ?: FileManager.retrieve()

		val months: HashMap<Int, Pair<List<WorkSessionRaw>, Int>> = hashMapOf()
		for (i in 1..12) {
			val hourlyWage: Int? = workYear.months[i]?.hourlyWage
			val sessions: List<WorkSession>? = workYear.months[i]?.sessions?.toList()

			if (hourlyWage != null && sessions != null) {
				months[i] = Pair(sessions.map { it.getRawData() }, hourlyWage)
			}
		}

		val data: Pair<Int, HashMap<Int, Pair<List<WorkSessionRaw>, Int>>> = Pair(workYear.year, months)
		jacksonObjectMapper().writeValue(file, data)
	}

	/**
	 * @param monthNumber implicitly set to the currently opened month
	 * @return WorkMonth instance of the given month
	 */
	fun getMonth(monthNumber: Int = DataHolder.currentMonth): WorkMonth {
		val month = workYear.months[monthNumber]

		return if (monthNumber in 1..12 && month != null) {
			month
		} else {
			throw IllegalArgumentException("Month numbers can only be between 1 and 12!")
		}
	}
}