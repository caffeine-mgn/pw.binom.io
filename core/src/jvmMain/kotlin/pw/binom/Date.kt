package pw.binom

import java.util.*
import java.util.Date as JDate


actual class Date {

    private val native: JDate

    actual constructor(time: Long) {
        native = JDate(time)
    }

    internal actual constructor(year: Int, month: Int, dayOfMonth: Int, hours: Int, min: Int, sec: Int) {
        native = JDate(year, month, dayOfMonth, hours, min, sec)
    }

    actual val year: Int
        get() = native.year
    actual val month: Int
        get() = native.month
    actual val dayOfMonth: Int
        get() = native.date
    actual val hours: Int
        get() = native.hours
    actual val min: Int
        get() = native.minutes
    actual val sec: Int
        get() = native.seconds
    actual val time: Long
        get() = native.time

    actual companion object {
        actual fun now(): Date = Date(JDate().time)
        actual val timeZoneOffset: Int
            get() {
                val r = TimeZone.getDefault().rawOffset
                if (r == 0)
                    return 0
                return r / 1000 / 60
            }
    }

    actual val dayOfWeek: Int
        get() {
            val calendar = Calendar.getInstance()
            calendar.time = native
            return calendar.get(Calendar.DAY_OF_WEEK)
        }

}