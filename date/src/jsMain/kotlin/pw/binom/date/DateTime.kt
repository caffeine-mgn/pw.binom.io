package pw.binom.date

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
            timeZoneOffset: Int
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
}
