package com.duno.workmanager.Models

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File


/**
 * Package: com.duno.workmanager.Data
 * Created by Daniel Zvir on 16.5.17.
 */
class WorkYear() {
    private val mapper = jacksonObjectMapper()
    val months = hashMapOf<Int, MutableList<WorkSession>>()

    init {
        for (i in 1..12) this.months[i] = mutableListOf()
    }

    constructor(yearInJson: String) : this() {
        val rawData: HashMap<Int, MutableList<WorkSessionRaw>> = mapper.readValue(yearInJson)
        for (i in 1..12) {
            rawData[i]?.forEach {
                months[i]?.add(WorkSession(it))
            }
        }
    }

    constructor(saveFile: File) : this() {
        val rawData: HashMap<Int, MutableList<WorkSessionRaw>> = mapper.readValue(saveFile)
        for (i in 1..12) {
            rawData[i]?.forEach {
                months[i]?.add(WorkSession(it))
            }
        }
    }

    /**
     * @return Map of raw data containing WorkSessions
     */
    fun getMapOfRawWorkSessions(): HashMap<Int, MutableList<WorkSessionRaw>> {
        val monthsRaw = hashMapOf<Int, MutableList<WorkSessionRaw>>()
        for (i in 1..12) {
            monthsRaw[i] = mutableListOf()
            months[i]?.forEach { monthsRaw[i]?.add(it.rawData) }
        }
        return monthsRaw
    }

    /**
     * @return String containing JSON with a HashMap of WorkSessionRaw
     */
    fun getYearInJson(): String = mapper.writeValueAsString(getMapOfRawWorkSessions())

    /**
     * @param saveFile File to save all the data into, example: "result.json"
     * @return false if exception is thrown, true otherwise
     */
    fun writeYearInJson(saveFile: File): Boolean {
        try {
            mapper.writeValue(saveFile, getMapOfRawWorkSessions())
        } catch (e: Exception) {
            return false
        }

        return true
    }

    /**
     * @param month index
     * @param session WorkSession to be added to a given month
     */
    fun addToMonth(month: Int, session: WorkSession) {
        checkMonthNumber(month)
        months[month]?.add(session)
    }

    /**
     * @param sessions Collection of WorkSessions to be added to a given month
     * @param month index
     */
    fun addAllToMonth(month: Int, sessions: Collection<WorkSession>) {
        checkMonthNumber(month)
        months[month]?.addAll(sessions)
    }

    /**
     * @return List of WorkSessions for a given month, month index is checked
     */
    fun getMonth(month: Int): MutableList<WorkSession> {
        checkMonthNumber(month)
        return months[month] ?: mutableListOf()
    }

    /**
     * Clears data for one month
     */
    fun clearMonth(month: Int) {
        checkMonthNumber(month)
        months[month]?.clear()
    }

    /**
     * Clears all data for all visibleDataMap
     */
    fun clearYear() {
        for (month in months) {
            month.value.clear()
        }
    }

    /**
     * @throws IllegalArgumentException if given month is not between 1 and 12
     */
    private fun checkMonthNumber(month: Int) = if (month !in 1..12) throw IllegalArgumentException("Months must be between 1 and 12!") else null

    /**
     * @return Total profit for a given month, without currency
     */
    fun getMonthProfit(month: Int): Double {
        checkMonthNumber(month)
        var sum = 0.0
        months[month]?.forEach {
            val profit = it.durationInMinutes * (it.hourlyWage.toDouble() / 60.0)
            sum += profit
        }

        return sum
    }

    /**
     * @return Total amount of hours in a given month
     */
    fun getMonthTotalHours(month: Int): Double {
        checkMonthNumber(month)
        var result = 0.0
        months[month]?.forEach {
            result += it.durationInMinutes / 60.0
        }

        return result
    }

    /**
     * @return Total profit for a given year, without currency
     */
    fun getYearProfit(): Double {
        var result = 0.0
        months.forEach { result += getMonthProfit(it.key) }
        return result
    }

    /**
     * @return Total amount of hours in a year
     */
    fun getYearTotalHours(): Double {
        var result = 0.0
        months.forEach { result += (getMonthTotalHours(it.key)) }
        return result
    }
}