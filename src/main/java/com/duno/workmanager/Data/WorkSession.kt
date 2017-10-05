package com.duno.workmanager.Data

import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Daniel Zvir on 15.5.17.
 */

/**
 * @param beginDate java.util.Date containing time when the session started
 * @param endDate java.util.Date containing time when the session ended
 * @param hourlyWage Without any currency
 * @param description Preferred length is 90 characters or less
 */
data class WorkSessionRaw(var beginDate: Date, var endDate: Date,
                          var hourlyWage: Int, var description: String)

open class WorkSession {
    var beginDate: Date
    var endDate: Date
    var hourlyWage: Int
    var description: String

    val rawData get() = WorkSessionRaw(beginDate, endDate, hourlyWage, description)

    /**
     * Calculates duration and converts it to minutes (double)
     */
    val durationInMinutes get() = Duration.between(beginDate.toInstant(), endDate.toInstant()).toMinutes().toDouble()

    /**
     * @param beginDate Date when the session started, implicitly set to Instant.now()
     * @param endDate Date when the session ended
     * @param hourlyWage Without any currency
     * @param description Preferred length is 90 characters or less, implicitly set to "Empty description"
     */
    constructor(beginDate: Date = Date.from(Instant.now()),
                endDate: Date, hourlyWage: Int,
                description: String = "Empty description") {
        this.beginDate = beginDate
        this.endDate = endDate
        this.hourlyWage = hourlyWage
        this.description = description
    }

    /**
     * @param beginDate Date when the session started, implicitly set to Instant.now()
     * @param addMinutes Creates an endDate (java.util.Date) from beginDate + minutes you specified
     * @param hourlyWage Without any currency
     * @param description Preferred length is 90 characters or less, implicitly set to "Empty description"
     */
    constructor(beginDate: Date = Date.from(Instant.now()),
                addMinutes: Long, hourlyWage: Int,
                description: String = "Empty description") {
        this.beginDate = beginDate
        this.endDate = Date(beginDate.time + TimeUnit.MINUTES.toMillis(addMinutes))
        this.hourlyWage = hourlyWage
        this.description = description
    }

    /**
     * @param beginDate Date when the session started, implicitly set to Instant.now()
     * @param addHours Creates an endDate (java.util.Date) from beginDate + hours you specified
     * @param hourlyWage Without any currency
     * @param description Preferred length is 90 characters or less, implicitly set to "Empty description"
     */
    constructor(beginDate: Date = Date.from(Instant.now()),
                addHours: Double, hourlyWage: Int,
                description: String = "Empty description") {
        this.beginDate = beginDate
        this.endDate = Date(beginDate.time + TimeUnit.MINUTES.toMillis((addHours * 60).toLong()))
        this.hourlyWage = hourlyWage
        this.description = description
    }

    /**
     * @param raw Creates new WorkSession from any raw data
     */
    constructor(raw: WorkSessionRaw) : this(raw.beginDate, raw.endDate,
            raw.hourlyWage, raw.description)

    /**
     * @param workSession Creates new WorkSession using its raw data
     */
    constructor(workSession: WorkSession) : this(workSession.rawData)
}