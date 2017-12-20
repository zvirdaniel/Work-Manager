package cz.zvird.workmanager.models

import java.util.*

/**
 * @param beginDate containing time when the session started
 * @param endDate time when the session ended
 * @param hourlyWage without any currency
 * @param description preferred length is 90 characters or less
 */
data class WorkSessionRaw(var beginDate: Date, var endDate: Date,
                          var hourlyWage: Int, var description: String)