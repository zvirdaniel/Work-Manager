package com.duno.workmanager.Data

import org.testng.Assert.assertEquals
import org.testng.annotations.Test

/**
 * Package: com.duno.workmanager
 * Created by Daniel Zvir on 14.5.17.
 */
class WorkSessionTest {
    val thirtyMinSession = WorkSession(addMinutes = 30, hourlyWage = 80)
    val twoHourSession = WorkSession(addMinutes = 120, hourlyWage = 100, description = "Popis s českými znaky ěščřžýáíéúů")

    @Test
    fun testGetDescription() {
        assertEquals(twoHourSession.description, "Popis s českými znaky ěščřžýáíéúů")
        assertEquals(thirtyMinSession.description, "Empty description")
    }

    @Test
    fun testGetTimeElapsed() {
        assertEquals(thirtyMinSession.duration.toMinutes().toInt(), 30)
        assertEquals(twoHourSession.duration.toHours().toInt(), 2)
    }

    @Test
    fun testGetTotalProfit() {
        assertEquals(twoHourSession.profit, 200.0)
        assertEquals(thirtyMinSession.profit, 40.0)
    }

    @Test
    fun testGetHourlyWage() {
        assertEquals(thirtyMinSession.hourlyWage, 80)
        assertEquals(twoHourSession.hourlyWage, 100)
    }

    @Test
    fun testRawDataChange() {
        val temporaryWork = thirtyMinSession
        temporaryWork.endDate = twoHourSession.endDate
        val rawEndDate = temporaryWork.rawData.endDate
        assertEquals(twoHourSession.endDate, rawEndDate)
    }
}