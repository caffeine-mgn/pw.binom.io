package pw.binom.date

import kotlin.jvm.JvmInline

@JvmInline
expect value class Date(val time: Long = now) {
    companion object {
        val timeZoneOffset: Int
        val now: Long

        /**
         * @param year full year. For example 2010
         * @param month Month, from 1 (January) to 12 (December)
         * @param timeZoneOffset Timezone offset in minutes
         */
        internal fun internalOf(
            year: Int,
            month: Int,
            dayOfMonth: Int,
            hours: Int,
            minutes: Int,
            seconds: Int,
            millis: Int,
            timeZoneOffset: Int
        ): Date
    }

    fun calendar(timeZoneOffset: Int = Date.timeZoneOffset): Calendar
}

fun Date.Companion.of(calendar: Calendar) =
    of(
        year = calendar.year,
        month = calendar.month,
        dayOfMonth = calendar.dayOfMonth,
        hours = calendar.hours,
        minutes = calendar.minutes,
        seconds = calendar.seconds,
        millis = calendar.millisecond,
        timeZoneOffset = calendar.timeZoneOffset,
    )

fun Date.Companion.of(
    year: Int,
    month: Int,
    dayOfMonth: Int,
    hours: Int,
    minutes: Int,
    seconds: Int,
    millis: Int,
    timeZoneOffset: Int = 0
): Date {
    require(month >= 1 && month <= 12) { "Invalid value of month. Valid values 1-12" }
    require(millis >= 0 && millis <= 999) { "Invalid value of millis. Valid values 0-999" }
    return internalOf(year, month, dayOfMonth, hours, minutes, seconds, millis, timeZoneOffset)
}
