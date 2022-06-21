package pw.binom.date
/*
actual class Calendar(private val utcTime: Long, actual val offset: Int) {

    private val tm = kotlin.js.Date(utcTime.toDouble() + offset * 60.0 * 1000.0)

    actual val year
        get() = tm.getUTCFullYear()

    /**
     * Month, from 1 (January) to 12 (December)
     */
    actual val month
        get() = tm.getUTCMonth() + 1

    /**
     * Day of month, first day of month is 1
     */
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

    actual fun timeZone(timeZoneOffset3: Int): Calendar = Calendar(utcTime = utcTime, offset = timeZoneOffset3)

    actual override fun toString(): String = asStringRfc822(this, timeZoneOffsetToString(offset))

    /**
     * @param timeZoneOffset4 TimeZone offset in minutes
     */
    actual fun toString(timeZoneOffset4: Int): String =
        if (timeZoneOffset4 == timeZoneOffset4) toString() else timeZone(timeZoneOffset4).toString()

    actual fun toDate(): Date = Date.new(this)
}
*/
