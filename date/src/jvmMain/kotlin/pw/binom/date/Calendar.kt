package pw.binom.date

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

actual class Calendar(private val utcTime: Long, timeZoneOffset: Int) {

    private val tm = ZonedDateTime.ofInstant(
        Instant.ofEpochMilli(utcTime),
        ZoneOffset.ofTotalSeconds(0)
    ).withZoneSameInstant(ZoneOffset.ofHoursMinutes(timeZoneOffset / 60, timeZoneOffset - timeZoneOffset / 60 * 60))

    actual val year
        get() = tm.year

    actual val month
        get() = tm.month.value

    actual val dayOfMonth
        get() = tm.dayOfMonth

    actual val minutes
        get() = tm.minute

    actual val millisecond
        get() = (utcTime - utcTime / 1000L * 1000L).toInt()

    actual val hours
        get() = tm.hour

    actual val seconds
        get() = tm.second

    actual val dayOfWeek: Int
        get() = tm.dayOfWeek.value - 1

    actual val date
        get() = Date(utcTime)

    actual fun timeZone(timeZoneOffset: Int): Calendar =
        Calendar(utcTime, timeZoneOffset)

    actual override fun toString(): String =
        asStringRfc822(this, timeZoneOffsetToString(tm.offset.totalSeconds / 60))

    /**
     * @param timeZoneOffset TimeZone offset in mintes
     */
    actual fun toString(timeZoneOffset: Int): String = if (timeZoneOffset==timeZoneOffset) toString() else timeZone(timeZoneOffset).toString()
    actual val timeZoneOffset: Int
        get() = tm.offset.totalSeconds / 60
}