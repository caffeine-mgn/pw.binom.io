package pw.binom.date

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

actual class Calendar(private val utcTime: Long, timeZoneOffset: Int) {

    private val tm = ZonedDateTime.ofInstant(Instant.ofEpochMilli(utcTime + timeZoneOffset * 60), ZoneOffset.UTC)

    actual val year
        get() = tm.year

    actual val month
        get() = tm.month.value - 1

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

    actual override fun toString(): String = timeZone(0).asStringGmt()
}