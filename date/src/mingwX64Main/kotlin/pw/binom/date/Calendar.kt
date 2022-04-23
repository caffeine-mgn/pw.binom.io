package pw.binom.date

import kotlinx.cinterop.*
import platform.posix.localtime_r
import platform.posix.time_tVar
import platform.posix.tm

actual class Calendar(private val utcTime: Long, actual val offset: Int) {

    private var tm_year: Int = 0
    private var tm_hour: Int = 0
    private var tm_mday: Int = 0
    private var tm_mon: Int = 0
    private var tm_min: Int = 0
    private var tm_sec: Int = 0
    private var tm_wday: Int = 0

    init {
        println("utcTime: $utcTime")
        memScoped {
            val dateTime = alloc<tm>()
            val timeSec = alloc<time_tVar>()
            val tx = offset - Date.systemZoneOffset
            timeSec.value = (utcTime / 1000L + tx * 60L).convert()
            if (localtime_r(timeSec.ptr, dateTime.ptr) == null) {
                throw IllegalArgumentException("Can't convert $utcTime to Calendar")
            }
            tm_year = dateTime.tm_year
            tm_hour = dateTime.tm_hour
            tm_mday = dateTime.tm_mday
            tm_mon = dateTime.tm_mon
            tm_min = dateTime.tm_min
            tm_sec = dateTime.tm_sec
            tm_wday = dateTime.tm_wday
        }

    }

    actual val year
        get() = tm_year + 1900

    /**
     * Month, from 1 (January) to 12 (December)
     */
    actual val month
        get() = tm_mon + 1

    /**
     * Day of month, first day of month is 1
     */
    actual val dayOfMonth
        get() = tm_mday

    actual val minutes
        get() = tm_min

    actual val millisecond
        get() = (utcTime - utcTime / 1000L * 1000L).toInt()

    actual val hours
        get() = tm_hour

    actual val seconds
        get() = tm_sec

    actual val date
        get() = Date(utcTime)

    actual val dayOfWeek: Int
        get() = tm_wday

    actual fun timeZone(timeZoneOffset3: Int): Calendar = Calendar(utcTime = utcTime, offset = timeZoneOffset3)

    actual override fun toString(): String =
        asStringRfc822(this, timeZoneOffsetToString(offset))

    actual fun toString(timeZoneOffset4: Int): String = timeZone(timeZoneOffset4).toString()
    actual fun toDate(): Date = Date.new(this)
}