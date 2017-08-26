package com.duno.workmanager.Models

import com.duno.workmanager.Data.WorkSession
import com.duno.workmanager.Other.errorNotification
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Daniel Zvir on 16.08.2017.
 */
class ObservableSession(session: WorkSession) : WorkSession(session) {
    // Date with the following format: dd. MM. yyyy
    val beginDateProperty: StringProperty
        get() = SimpleStringProperty(beginDateString)

    var beginDateString: String
        get() = SimpleDateFormat("dd. MM. yyyy").format(beginDate)
        set(date) {
            var parsedDate: Date? = null

            try {
                parsedDate = SimpleDateFormat("dd. MM. yyyy").parse(date)
            } catch (e: Exception) {
                errorNotification("Error while parsing $date")
                return
            }

            if (parsedDate != null) {
                beginDate = parsedDate
                println("Begin date - ${DateFormat.getInstance().format(beginDate)} was set.")
            }
        }


    // Time with the following format: HH:mm
    val beginTimeProperty: StringProperty
        get() = SimpleStringProperty(beginTimeString)

    var beginTimeString: String
        get() = SimpleDateFormat("HH:mm").format(beginDate)
        set(time) {
            if (time == "00:00" || time == "24:00") {
                errorNotification("Time can't be set to midnight!")
                return
            }

            var parsedDate: Date? = null

            try {
                parsedDate = SimpleDateFormat("dd. MM. yyyy HH:mm").parse(beginDateString + " " + time)
            } catch (e: Exception) {
                errorNotification("Error while parsing $time.")
                return
            }

            if (parsedDate != null) {
                beginDate = parsedDate
                println("Begin date - ${DateFormat.getInstance().format(beginDate)} was set.")
            }
        }


    // Description
    val descriptionProperty: StringProperty
        get() = SimpleStringProperty(descriptionString)

    var descriptionString: String
        get() = description
        set(value) {
            if (value.length > 90) {
                val overflow = value.length - 90
                errorNotification("Description length owerflow by $overflow characters.")
            } else {
                description = value
            }
        }


    // Profit without currency
    val profitProperty: StringProperty
        get() = SimpleStringProperty(profitString)

    val profitString: String
        get() {
            val duration = Duration.between(beginDate.toInstant(), endDate.toInstant()).toMinutes()
            return (duration.toDouble() * (hourlyWage.toDouble() / 60.0)).toString()
        }


    // Duration in hours
    val durationProperty: StringProperty
        get() = SimpleStringProperty(durationString)

    var durationString: String
        get() = Duration.between(beginDate.toInstant(), endDate.toInstant()).toHours().toString()
        set(text) {
            val duration = Duration.ofHours(text.toLong()).toHours()
            endDate = Date(beginDate.time + TimeUnit.MINUTES.toMillis(duration * 60))
            println("End date - ${DateFormat.getInstance().format(endDate)} was set.")
        }
}