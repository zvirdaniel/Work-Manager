package com.duno.workmanager.Models

import com.duno.workmanager.Data.DataHolder
import com.duno.workmanager.Models.Properties.LocalDateProperty
import javafx.beans.property.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * @param beginDateTime Date when the session started
 * @param duration of the session
 * @param hourlyWage Without any currency
 * @param description Preferred length is 90 characters or less, implicitly set to "Empty description"
 */
class WorkSession(beginDateTime: LocalDateTime,
                  duration: Duration,
                  hourlyWage: Int,
                  description: String) {
    val beginDateProperty: LocalDateProperty
    val beginTimeProperty: ObjectProperty<LocalTime>
    val durationProperty: ObjectProperty<Duration>
    val hourlyWageProperty: IntegerProperty
    val descriptionProperty: StringProperty

    init {
        this.beginDateProperty = LocalDateProperty(beginDateTime.toLocalDate())
        this.beginTimeProperty = SimpleObjectProperty(beginDateTime.toLocalTime())
        this.durationProperty = SimpleObjectProperty(duration)
        this.hourlyWageProperty = SimpleIntegerProperty(hourlyWage)
        this.descriptionProperty = SimpleStringProperty(description)
    }

    /**
     * Generates WorkSessionRaw to enable simple data saving
     */
    fun getRawData(): WorkSessionRaw {
        val beginDateTime = LocalDateTime.of(beginDateProperty.get(), beginTimeProperty.get())
        val beginDate = Date.from(beginDateTime.atZone(DataHolder.zone).toInstant())
        val endDateTime = beginDateTime.plusMinutes(durationProperty.get().toMinutes())
        val endDate = Date.from(endDateTime.atZone(DataHolder.zone).toInstant())
        return WorkSessionRaw(beginDate, endDate, hourlyWageProperty.get(), descriptionProperty.get())
    }


    /**
     * @param beginDateTime Date when the session started
     * @param endDateTime Date when the session ended
     * @param hourlyWage Without any currency
     * @param description Preferred length is 90 characters or less, implicitly set to "Empty description"
     */
    constructor(beginDateTime: LocalDateTime = LocalDateTime.now(),
                endDateTime: LocalDateTime,
                hourlyWage: Int,
                description: String = "Empty descriptionProperty")
            : this(beginDateTime, Duration.between(beginDateTime, endDateTime), hourlyWage, description)

    /**
     * @param beginDateTime Date when the session started
     * @param addMinutes Duration in minutes
     * @param hourlyWage Without any currency
     * @param description Preferred length is 90 characters or less, implicitly set to "Empty description"
     */
    constructor(beginDateTime: LocalDateTime = LocalDateTime.now(),
                addMinutes: Long,
                hourlyWage: Int,
                description: String = "Empty descriptionProperty")
            : this(beginDateTime, Duration.between(beginDateTime, LocalDateTime.from(beginDateTime).plusMinutes(addMinutes)), hourlyWage, description)

    /**
     * @param raw Creates new WorkSession from any raw data
     */
    constructor(raw: WorkSessionRaw) : this(
            LocalDateTime.ofInstant(raw.beginDate.toInstant(), DataHolder.zone),
            LocalDateTime.ofInstant(raw.endDate.toInstant(), DataHolder.zone),
            raw.hourlyWage,
            raw.description
    )

    /**
     * @param workSession Creates new WorkSession using its raw data
     */
    constructor(workSession: WorkSession) : this(workSession.getRawData())
}