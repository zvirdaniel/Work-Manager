package cz.zvird.workmanager.models

import cz.zvird.workmanager.data.DataHolder
import javafx.beans.property.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * @param beginDateTime date when the session started
 * @param duration of the session
 * @param hourlyWage without any currency
 * @param description preferred length is 90 characters or less, implicitly set to empty string
 */
class WorkSession(beginDateTime: LocalDateTime,
                  duration: Duration,
                  hourlyWage: Int,
                  description: String) {
    val beginDateProperty: ObjectProperty<LocalDate>
    val beginTimeProperty: ObjectProperty<LocalTime>
    val durationProperty: ObjectProperty<Duration>
    val hourlyWageProperty: IntegerProperty
    val descriptionProperty: StringProperty

    init {
        this.beginDateProperty = SimpleObjectProperty(beginDateTime.toLocalDate())
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
     * @param beginDateTime date when the session started
     * @param endDateTime date when the session ended
     * @param hourlyWage without any currency
     * @param description preferred length is 90 characters or less, implicitly set to empty string
     */
    constructor(beginDateTime: LocalDateTime = LocalDateTime.now(DataHolder.zone),
                endDateTime: LocalDateTime,
                hourlyWage: Int,
                description: String = "")
            : this(beginDateTime, Duration.between(beginDateTime, endDateTime), hourlyWage, description)

    /**
     * @param beginDateTime date when the session started
     * @param addMinutes duration in minutes
     * @param hourlyWage without any currency
     * @param description preferred length is 90 characters or less, implicitly set to empty string
     */
    constructor(beginDateTime: LocalDateTime = LocalDateTime.now(DataHolder.zone),
                addMinutes: Long,
                hourlyWage: Int,
                description: String = "")
            : this(beginDateTime, Duration.between(beginDateTime, LocalDateTime.from(beginDateTime).plusMinutes(addMinutes)), hourlyWage, description)

    /**
     * Creates new WorkSession from any raw data
     * @param raw data
     */
    constructor(raw: WorkSessionRaw) : this(
            LocalDateTime.ofInstant(raw.beginDate.toInstant(), DataHolder.zone),
            LocalDateTime.ofInstant(raw.endDate.toInstant(), DataHolder.zone),
            raw.hourlyWage,
            raw.description
    )

    /**
     * Creates new WorkSession using its raw data
     * @param workSession data
     */
    constructor(workSession: WorkSession) : this(workSession.getRawData())
}