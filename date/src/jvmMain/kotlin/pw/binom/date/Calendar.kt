@file:JvmName("CalendarUtilsKt")

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

    /**
     * Month, from 1 (January) to 12 (December)
     */
    actual val month
        get() = tm.month.value

    /**
     * Day of month, first day of month is 1
     */
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
        get() = javaDayOfWeekToCommon(tm.dayOfWeek.value)

    actual val dateTime
        get() = DateTime(utcTime)

    actual fun timeZone(timeZoneOffset3: Int): Calendar =
        Calendar(utcTime = utcTime, timeZoneOffset = timeZoneOffset3)

    actual override fun toString(): String =
        asStringRfc822(this, timeZoneOffsetToString(tm.offset.totalSeconds / 60))

    /**
     * @param timeZoneOffset4 TimeZone offset in mintes
     */
    actual fun toString(timeZoneOffset4: Int): String =
        if (timeZoneOffset4 == timeZoneOffset4) toString() else timeZone(timeZoneOffset4).toString()

    actual val offset: Int
        get() = tm.offset.totalSeconds / 60

    actual fun toDate(): DateTime = DateTime.new(this)

    actual companion object
}

internal fun javaDayOfWeekToCommon(day: Int) =
    when (day) {
        7 -> 0
        1 -> 1
        2 -> 2
        3 -> 3
        4 -> 4
        5 -> 5
        6 -> 6
        else -> throw IllegalArgumentException("Invalid day of week $day")
    }

internal fun commonDayOfWeekToJava(day: Int) =
    when (day) {
        0 -> 7
        1 -> 1
        2 -> 2
        3 -> 3
        4 -> 4
        5 -> 5
        6 -> 6
        else -> throw IllegalArgumentException("Invalid day of week $day")
    }
