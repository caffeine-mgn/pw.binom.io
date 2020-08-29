package pw.binom.date

actual class Calendar(private val utcTime: Long, timeZoneOffset: Int) {


    private val tm = kotlin.js.Date(utcTime.toDouble() + timeZoneOffset * 60.0 * 1000.0)

    actual val year
        get() = tm.getUTCFullYear()

    actual val month
        get() = tm.getUTCMonth()

    actual val dayOfMonth
        get() = tm.getUTCDate()

    actual val minutes
        get() = tm.getUTCMinutes()

    actual val millisecond
        get() = tm.getUTCMilliseconds()

    actual val hours
        get() = tm.getUTCHours()

    actual val seconds
        get() = tm.getUTCSeconds()

    actual val dayOfWeek: Int
        get() = tm.getUTCDay()

    actual val date
        get() = Date(utcTime)

    actual fun timeZone(timeZoneOffset: Int): Calendar = Calendar(utcTime, timeZoneOffset)

    actual override fun toString(): String = timeZone(0).asStringGmt()
}