//package com.duno.workmanager.Models
//
//import com.duno.workmanager.gui.errorNotification
//import com.duno.workmanager.gui.isOnSameDay
//import javafx.beans.property.SimpleStringProperty
//import javafx.beans.property.StringProperty
//import java.text.DateFormat
//import java.text.SimpleDateFormat
//import java.time.Duration
//import java.util.*
//import java.util.concurrent.TimeUnit
//
//class ObservableSession(session: WorkSession) : WorkSession(session) {
//    /**
//     * @return date with the following format: dd. MM. yyyy
//     */
//    val beginDateProperty: StringProperty
//        get() = SimpleStringProperty(beginDateString)
//
//    var beginDateString: String
//        get() = SimpleDateFormat("dd. MM. yyyy").format(beginDateProperty)
//        set(date) {
//            val parsedDate: Date? = try {
//                SimpleDateFormat("dd. MM. yyyy").parse(date)
//            } catch (e: Exception) {
//                errorNotification("Chyba při parsování $date")
//                return
//            }
//
//            if (parsedDate != null) {
//                beginDateProperty = parsedDate
//                println("Begin date - ${DateFormat.getInstance().format(beginDateProperty)} was setPrimaryStage.")
//            }
//        }
//
//
//    /**
//     * @return time with the following format: HH:mm
//     */
//    val beginTimeProperty: StringProperty
//        get() = SimpleStringProperty(beginTimeString)
//
//    var beginTimeString: String
//        get() = SimpleDateFormat("HH:mm").format(beginDateProperty)
//        set(time) {
//            if (time == "00:00" || time == "24:00") {
//                errorNotification("Čas nelze nastavit na půlnoc!")
//                return
//            }
//
//            val parsedDate: Date? = try {
//                SimpleDateFormat("dd. MM. yyyy HH:mm").parse(beginDateString + " " + time)
//            } catch (e: Exception) {
//                errorNotification("Chyba při parsování $time.")
//                return
//            }
//
//            if (parsedDate != null) {
//                if (isOnSameDay(parsedDate, beginDateProperty)) {
//                    beginDateProperty = parsedDate
//                    println("Begin date - ${DateFormat.getInstance().format(beginDateProperty)} was setPrimaryStage.")
//                } else {
//                    errorNotification("$time není mezi 00:01 - 23:59")
//                }
//            }
//        }
//
//
//    /**
//     * @return description
//     */
//    val description: StringProperty
//        get() = SimpleStringProperty(descriptionString)
//
//    var descriptionString: String
//        get() = description
//        set(value) {
//            val MAX_LENGTH = 150
//            if (value.length > MAX_LENGTH) {
//                val overflow = value.length - MAX_LENGTH
//                errorNotification("Popis překročil limit o $overflow znaků.")
//            } else {
//                description = value
//            }
//        }
//
//
//    /**
//     * @return profit without currency
//     */
//    val profitProperty: StringProperty
//        get() = SimpleStringProperty(profitString)
//
//    val profitString: String
//        get() {
//            return (durationInMinutesCalc * (hourlyWageProperty.toDouble() / 60.0)).toString()
//        }
//
//
//    /**
//     * @return durationProperty in hours
//     */
//    val durationProperty: StringProperty
//        get() = SimpleStringProperty(durationString)
//
//    var durationString: String
//        get() {
//            val durationProperty = Duration.between(beginDateProperty.toInstant(), endDate.toInstant())
//            val hours = durationProperty.toMinutes() / 60.0
//            return hours.toString()
//        }
//        set(text) {
//            val durationInMinutes: Long?
//
//            durationInMinutes = try {
//                val minutes = text.toDouble() * 60
//                Duration.ofMinutes(minutes.toLong()).toMinutes()
//            } catch (e: Exception) {
//                errorNotification("Chyba při parsování $text.")
//                return
//            }
//
//            when {
//                durationInMinutes < 0 -> errorNotification("Nelze pracovat méně než 0 hodin!")
//                durationInMinutes > 18 * 60 -> errorNotification("Nejsi číňan, nelze pracovat více než 18 hodin!")
//                else -> {
//                    endDate = Date(beginDateProperty.time + TimeUnit.MINUTES.toMillis(durationInMinutes))
//                    println("End date - ${DateFormat.getInstance().format(endDate)} was setPrimaryStage.")
//                }
//            }
//        }
//}