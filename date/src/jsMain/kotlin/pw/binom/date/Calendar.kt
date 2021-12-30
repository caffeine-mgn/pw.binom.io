package pw.binom.date

actual class Calendar(private val utcTime: Long, actual val timeZoneOffset: Int) {


    private val tm = kotlin.js.Date(utcTime.toDouble() + timeZoneOffset * 60.0 * 1000.0)

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

    actual fun timeZone(timeZoneOffset: Int): Calendar = Calendar(utcTime, timeZoneOffset)

    actual override fun toString(): String = asStringRfc822(this, timeZoneOffsetToString(timeZoneOffset))

    /**
     * @param timeZoneOffset TimeZone offset in mintes
     */
    actual fun toString(timeZoneOffset: Int): String =
        if (timeZoneOffset == timeZoneOffset) toString() else timeZone(timeZoneOffset).toString()

    actual fun toDate(): Date = Date.new(this)
}