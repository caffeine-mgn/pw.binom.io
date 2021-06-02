package pw.binom.date

actual value class Date(val time: Long) {
    actual companion object {
        actual val timeZoneOffset: Int
            get() = js("new Date().getTimezoneOffset()")
        actual val nowTime: Long
            get() = js("new Date().getTime()").unsafeCast<Double>().toLong()

        actual fun internalOf(year: Int, month: Int, dayOfMonth: Int, hours: Int, minutes: Int, seconds: Int, millis: Int, timeZoneOffset: Int): Date {
            val date = kotlin.js.Date.UTC(year, month, dayOfMonth, hours, minutes, seconds, millis)
            return Date(date.toLong())
        }

        actual val now: Date
            get() = Date(nowTime)
    }

    actual fun calendar(timeZoneOffset: Int): Calendar =
            Calendar(time, timeZoneOffset)
}