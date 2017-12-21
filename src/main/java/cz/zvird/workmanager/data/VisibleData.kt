package cz.zvird.workmanager.data

import cz.zvird.workmanager.models.WorkSession
import cz.zvird.workmanager.models.WorkYear
import javafx.collections.FXCollections
import javafx.collections.ObservableList

/**
 * Manages data visible in the user interface
 */
object VisibleData {
	val visibleDataMap = hashMapOf<Int, ObservableList<WorkSession>>() // Holds the visible data of all months

	init {
		for (i in 1..12) visibleDataMap[i] = FXCollections.observableArrayList<WorkSession>()
		reloadCurrentFile()
	}

	/**
	 * Remaps data from the current file into visibleDataMap
	 */
	fun reloadCurrentFile() {
		val file = CurrentFile.get()
		val workYear = WorkYear(file)
		setAndShowCurrentWorkYear(workYear)
	}

	/**
	 * Creates a WorkYear from the data contained in the UI
	 * @return WorkYear containing the data from the UI
	 */
	fun generateWorkYearFromVisibleData(): WorkYear {
		val workYear = WorkYear()
		for ((key, value) in visibleDataMap) {
			workYear.addAllToMonth(key, value)
		}

		return workYear
	}

	/**
	 * Imports data from workYear into the visibleDataMap = into UI
	 * @param workYear used as data source for import, it's mapped into visibleDataMap
	 */
	private fun setAndShowCurrentWorkYear(workYear: WorkYear) {
		for ((key, value) in workYear.months) {
			visibleDataMap[key]?.clear()
			visibleDataMap[key]?.addAll(value.map(::WorkSession))
		}
	}
}