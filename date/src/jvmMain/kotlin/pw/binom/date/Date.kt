package pw.binom.date

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.min

actual inline class Date(val time: Long = now) {
    actual companion object {
        actual val timeZoneOffset: Int
            get() = TimeZone.getDefault().rawOffset / 1000 / 60

        actual val now: Long
            get() = System.currentTimeMillis()

        actual fun internalOf(
            year: Int,
            month: Int,
            dayOfMonth: Int,
            hours: Int,
            minutes: Int,
            seconds: Int,
            millis: Int,
            timeZoneOffset: Int
        ): Date {
            return Date(
                ZonedDateTime.of(
                    year,
                    month,
                    dayOfMonth,
                    hours,
                    minutes,
                    seconds,
                    millis * 1000,
                    ZoneOffset.ofHoursMinutes(timeZoneOffset / 60, timeZoneOffset - (timeZoneOffset / 60 * 60))
                ).toInstant().toEpochMilli()
            )


//
//            val t = LocalDateTime.of(
//                year,
//                month,
//                dayOfMonth,
//                hours,
//                minutes,
//                seconds,
//                millis
//            )
//            val v = t.atZone(ZoneOffset.UTC)
//                .toInstant()
//            return Date(v.toEpochMilli() - (timeZoneOffset * 60L * 1000L))
        }
    }

    actual fun calendar(timeZoneOffset: Int): Calendar =
        Calendar(time, timeZoneOffset)
}