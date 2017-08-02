package com.duno.workmanager.Data

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.util.*


/**
 * Package: com.duno.workmanager.Data
 * Created by Daniel Zvir on 16.5.17.
 */
class WorkYear(val yearNumber: Int) {
    private val mapper = jacksonObjectMapper()
    private val months = hashMapOf<Int, MutableList<WorkSession>>()

    init {
        for (i in 1..12) this.months[i] = mutableListOf<WorkSession>()
    }

    constructor(yearNumber: Int, yearInJson: String) : this(yearNumber) {
        val rawData: HashMap<Int, MutableList<WorkSessionRaw>> = mapper.readValue(yearInJson)
        for (i in 1..12) {
            rawData[i]?.forEach {
                months[i]?.add(WorkSession(it))
            }
        }
    }

    constructor(yearNumber: Int, saveFile: File) : this(yearNumber) {
        val rawData: HashMap<Int, MutableList<WorkSessionRaw>> = mapper.readValue(saveFile)
        for (i in 1..12) {
            rawData[i]?.forEach {
                months[i]?.add(WorkSession(it))
            }
        }
    }

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
    fun getYearInJson() = mapper.writeValueAsString(getMapOfRawWorkSessions())

    /**
     * @param saveFile File to save all the data into, example: "result.json"
     */
    fun writeYearInJson(saveFile: File) = mapper.writeValue(saveFile, getMapOfRawWorkSessions())

    fun addToMonth(month: Int, session: WorkSession) {
        checkMonthNumber(month)
        months[month]?.add(session)
    }

    fun addAllToMonth(month: Int, sessions: Collection<WorkSession>) {
        checkMonthNumber(month)
        months[month]?.addAll(sessions)
    }

    fun getMonth(month: Int): MutableList<WorkSession> {
        checkMonthNumber(month)
        return months[month] ?: mutableListOf<WorkSession>()
    }

    /**
     * Clears data for one month
     */
    fun clearMonth(month: Int) {
        checkMonthNumber(month)
        months[month]?.clear()
    }

    /**
     * Clears all data for all months
     */
    fun clearYear() {
        for (month in months) {
            month.value.clear()
        }
    }

    private fun checkMonthNumber(month: Int) = if (month !in 1..12) throw IllegalArgumentException("Months must be between 1 and 12!") else null

    fun getMonthProfit(month: Int): Double {
        checkMonthNumber(month)
        var result = 0.0
        months[month]?.forEach {
            result += it.profit
        }
        return result;
    }

    fun getMonthTotalHours(month: Int): Double {
        checkMonthNumber(month)
        var result = 0.0
        months[month]?.forEach {
            result += it.duration.toMinutes().toDouble() / 60.0
        }
        return result;
    }

    fun getYearProfit(): Double {
        var result: Double = 0.0
        months.forEach { result += getMonthProfit(it.key) }
        return result
    }

    fun getYearTotalHours(): Double {
        var result = 0.0
        months.forEach { result += (getMonthTotalHours(it.key)) }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as WorkYear

        if (yearNumber != other.yearNumber) return false
        if (months != other.months) return false

        return true
    }

    override fun hashCode(): Int {
        var result = yearNumber
        result = 31 * result + months.hashCode()
        return result
    }

    override fun toString(): String {
        return "WorkYear(" +
                "yearNumber=$yearNumber," +
                "months=$months" +
                ")"
    }
}