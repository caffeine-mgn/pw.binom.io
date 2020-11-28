package pw.binom.date

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

actual inline class Date(val time: Long) {
    actual companion object {
        actual val timeZoneOffset: Int
            get() = TimeZone.getDefault().rawOffset / 1000 / 60
        actual val now: Long
            get() = System.currentTimeMillis()

        actual fun internalOf(year: Int, month: Int, dayOfMonth: Int, hours: Int, minutes: Int, seconds: Int, millis: Int, timeZoneOffset: Int): Date {
            val t = LocalDateTime.of(
                    year,
                    month + 1,
                    dayOfMonth,
                    hours,
                    minutes,
                    seconds,
                    millis
            )
            val v = t.atZone(ZoneOffset.UTC)
                    .toInstant()
            return Date((v.epochSecond - timeZoneOffset * 60L) * 1000L + v.nano)
        }
    }

    actual fun calendar(timeZoneOffset: Int): Calendar =
            Calendar(time, timeZoneOffset)
}