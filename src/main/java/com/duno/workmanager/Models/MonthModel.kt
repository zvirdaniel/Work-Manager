package com.duno.workmanager.Models

import com.duno.workmanager.Data.WorkSession
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Daniel Zvir on 26.7.17.
 */
class MonthModel(workSession: WorkSession) {
    var date: ObjectProperty<Date>
    var beginTime: StringProperty
    var duration: StringProperty
    var description: StringProperty

    init {
        this.date = SimpleObjectProperty<Date>(workSession.beginDate)
        this.beginTime = SimpleStringProperty(SimpleDateFormat("HH:mm").format(workSession.beginDate).toString())
        this.duration = SimpleStringProperty(workSession.duration.toHours().toString())
        this.description = SimpleStringProperty(workSession.description)
    }
}