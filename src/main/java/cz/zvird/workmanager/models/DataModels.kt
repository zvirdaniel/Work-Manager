package cz.zvird.workmanager.models

import cz.zvird.workmanager.data.DataHolder
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.ObservableList
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class WorkSession(beginDateTime: LocalDateTime,
                  duration: Duration,
                  description: String) {
	val beginDateProperty: ObjectProperty<LocalDate>
	val beginTimeProperty: ObjectProperty<LocalTime>
	val durationProperty: ObjectProperty<Duration>
	val descriptionProperty: StringProperty

	init {
		this.beginDateProperty = SimpleObjectProperty(beginDateTime.toLocalDate())
		this.beginTimeProperty = SimpleObjectProperty(beginDateTime.toLocalTime())
		this.durationProperty = SimpleObjectProperty(duration)
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
		return WorkSessionRaw(beginDate, endDate, descriptionProperty.get())
	}

	constructor(beginDateTime: LocalDateTime = LocalDateTime.now(DataHolder.zone),
	            endDateTime: LocalDateTime,
	            description: String = "")
			: this(beginDateTime, Duration.between(beginDateTime, endDateTime), description)

	constructor(beginDateTime: LocalDateTime = LocalDateTime.now(DataHolder.zone),
	            addMinutes: Long,
	            description: String = "")
			: this(beginDateTime, Duration.between(beginDateTime, LocalDateTime.from(beginDateTime).plusMinutes(addMinutes)), description)


	/**
	 * Creates new WorkSession from any raw data
	 * @param raw data
	 */
	constructor(raw: WorkSessionRaw) : this(
			LocalDateTime.ofInstant(raw.beginDate.toInstant(), DataHolder.zone),
			LocalDateTime.ofInstant(raw.endDate.toInstant(), DataHolder.zone),
			raw.description
	)
}

data class WorkSessionRaw(var beginDate: Date, var endDate: Date, var description: String)

data class WorkYear(var year: Int, val months: HashMap<Int, WorkMonth>)

data class WorkMonth(val sessions: ObservableList<WorkSession>, var hourlyWage: Int)