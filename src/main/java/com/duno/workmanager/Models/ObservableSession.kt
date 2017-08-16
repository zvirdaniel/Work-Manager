package com.duno.workmanager.Models

import com.duno.workmanager.Data.WorkSession
import javafx.beans.property.SimpleStringProperty

/**
 * Created by Daniel Zvir on 26.7.17.
 */
class ObservableSession(workSession: WorkSession) {
    private val beginDateProperty = SimpleStringProperty(workSession.czechBeginDate)
    private val beginTimeProperty = SimpleStringProperty(workSession.beginTime)
    private val durationProperty = SimpleStringProperty(workSession.duration.toHours().toString())
    private val descriptionProperty = SimpleStringProperty(workSession.description)

    var beginDate: String
        get() = beginDateProperty.get()
        set(value) = beginDateProperty.set(value)

    var beginTime: String
        get() = beginTimeProperty.get()
        set(value) = beginTimeProperty.set(value)

    var duration: String
        get() = durationProperty.get()
        set(value) = durationProperty.set(value)

    var description: String
        get() = descriptionProperty.get()
        set(value) = descriptionProperty.set(value)
}