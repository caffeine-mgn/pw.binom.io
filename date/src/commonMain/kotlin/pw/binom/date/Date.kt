package pw.binom.date

import kotlin.jvm.JvmInline

@JvmInline
expect value class Date(val time: Long = nowTime) {
    companion object {
        val systemZoneOffset: Int
        val nowTime: Long
        val now: Date

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

    fun calendar(timeZoneOffset: Int = Date.systemZoneOffset): Calendar
}

internal fun Date.Companion.new(calendar: Calendar) =
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
    year: Int = 1970,
    month: Int = 1,
    dayOfMonth: Int = 0,
    hours: Int = 0,
    minutes: Int = 0,
    seconds: Int = 0,
    millis: Int = 0,
    timeZoneOffset: Int = systemZoneOffset
): Date {
    require(month >= 1 && month <= 12) { "Invalid value of month. Valid values 1-12" }
    require(millis >= 0 && millis <= 999) { "Invalid value of millis. Valid values 0-999" }
    return internalOf(year, month, dayOfMonth, hours, minutes, seconds, millis, timeZoneOffset)
}

operator fun Date.compareTo(expDate: Date): Int = when {
    time > expDate.time -> 1
    time < expDate.time -> -1
    else -> 0
}