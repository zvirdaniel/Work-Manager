package com.duno.workmanager.Data

import org.testng.Assert.assertEquals
import org.testng.annotations.Test

/**
 * Package: com.duno.workmanager.Data
 * Created by Daniel Zvir on 14.5.17.
 */
class WorkYearTest {
    val workYear = WorkYear()

    init {
        val data = mutableListOf<WorkSession>(
                WorkSession(dateParse("02-05-2017 12:00"), 6.0, 100, "Testování různých řešení pro HTML prvek date, konzultace s Petrem ohledně různých JavaScript knihoven, srovnávání"),
                WorkSession(dateParse("03-05-2017 13:30"), 4.8, 100, "Analýza speciálu VOR a testování Pikaday knihovny, úprava knihovny pro univerzální použití"),
                WorkSession(dateParse("04-05-2017 12:00"), 8.0, 100, "Úprava speciálu VOR pro použití Pikaday knihovny, přepisování knihovny pro kompatibilitu s PHP"),
                WorkSession(dateParse("05-05-2017 15:30"), 4.4, 100, "VOR - dodělání Pikaday pro objednávky a faktury, odlehčení knihovny a komprese, pokus o univezální prvek"),
                WorkSession(dateParse("12-05-2017 16:00"), 4.0, 100, "Zařizování infoservisu, zkoušení různých skriptů na přidání prodlevy do aplikace kopírka. řešení potíží se synchronizací dat"),
                WorkSession(dateParse("15-05-2017 10:00"), 8.2, 100, "Přepisování aplikace kopírka do lehce použitelného stavu, přidání vybraného skriptu na prodlevu, testování a řešení chyb"),
                WorkSession(dateParse("16-05-2017 11:00"), 8.0, 100, "Úpravy HTML_Workflow pro Pavla, řešení chyb, plánování a přípravy dat pro Poptávky do e-shopu")
        )
        workYear.addAllToMonth(5, data)
    }

    @Test
    fun testYearTotalHours() {
        val hours = workYear.getYearTotalHours()
        assertEquals(Math.round(hours), 43)
    }

    @Test
    fun testSessionProfit() {
        val profit = workYear.getMonth(5).firstOrNull()?.profit
        assertEquals(profit, 600.0)
    }

    @Test
    fun testYearProfit() {
        val profit = workYear.getYearProfit()
        assertEquals(Math.round(profit), 4338)
    }

    @Test
    fun testMonthTotalHours() {
        val monthTotalHours = workYear.getMonthTotalHours(5)
        assertEquals(Math.round(monthTotalHours), 43)
    }
}