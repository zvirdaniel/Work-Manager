package com.duno.workmanager.Models

import com.duno.workmanager.Data.WorkSession
import java.time.Duration

/**
 * Created by Daniel Zvir on 16.08.2017.
 */
class ObservableSession(session: WorkSession) : WorkSession(session) {
    var beginDateProperty: String = czechBeginDate
        set(value) {
            czechBeginDate = value
        }

    var beginTimeProperty: String = beginTime
        set(value) {
            beginTime = value
        }

    var durationProperty: String = duration.toHours().toString()
        set(value) {
            duration = Duration.ofHours(value.toLong())
        }

    var descriptionProperty: String = description
        set(value) {
            description = value
        }
}