package pw.binom.date

import kotlin.time.Duration

actual value class DateTime(val time: Long = nowTime) {
    actual companion object {
        actual val systemZoneOffset: Int
            get() = -js("new Date().getTimezoneOffset()").unsafeCast<Int>()
        actual val nowTime: Long
            get() = js("Date.now()").unsafeCast<Double>().toLong()

        actual fun internalOf(
            year: Int,
            month: Int,
            dayOfMonth: Int,
            hours: Int,
            minutes: Int,
            seconds: Int,
            millis: Int,
            timeZoneOffset: Int,
        ): DateTime {
            val date = kotlin.js.Date.UTC(year, month - 1, dayOfMonth, hours, minutes, seconds, millis)
            val utc = date.toLong() - timeZoneOffset * 60 * 1000
            return DateTime(utc)
        }

        actual val now: DateTime
            get() = DateTime(nowTime)
    }

    actual fun calendar(timeZoneOffset: Int): Calendar =
        Calendar(utcTime = time, offset = timeZoneOffset)

    actual operator fun compareTo(expDate: DateTime): Int = dateTimeCompareTo(this, expDate)
    actual operator fun plus(duration: Duration) = dateTimePlus(date = this, duration = duration)
    actual operator fun minus(duration: Duration) = dateTimeMinus(date = this, duration = duration)
    actual operator fun minus(other: DateTime) = dateTimeMinus(
        date = this,
        other = other,
    )
}
