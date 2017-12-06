package cz.zvird.workmanager.models

import java.util.*

/**
 * @param beginDate containing time when the session started
 * @param endDate time when the session ended
 * @param hourlyWage Without any currency
 * @param description Preferred length is 90 characters or less
 */
data class WorkSessionRaw(var beginDate: Date, var endDate: Date,
                          var hourlyWage: Int, var description: String)