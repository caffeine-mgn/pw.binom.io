package pw.binom.date

import kotlin.math.absoluteValue

expect class Calendar {
    val year: Int

    /**
     * Month, from 1 (January) to 12 (December)
     */
    val month: Int
    val dayOfMonth: Int
    val dayOfWeek: Int
    val hours: Int
    val minutes: Int
    val seconds: Int
    val millisecond: Int
    val date: Date

    /**
     * Timezone offset in minutes
     */
    val timeZoneOffset: Int

    /**
     * Returns date as string in GMT Timezone
     */
    override fun toString(): String

    /**
     * @param timeZoneOffset TimeZone offset in mintes
     */
    fun toString(timeZoneOffset: Int): String

    /**
     * Changes current TimeZone.
     *
     * @param timeZoneOffset TimeZone offset in mintes
     */
    fun timeZone(timeZoneOffset: Int): Calendar
}

/**
 * @param offset Timezone Offset in minutes
 */
internal fun timeZoneOffsetToString(offset: Int): String {
    if (offset == 0) {
        return ""
    }
    val h = offset / 60
    val m = offset - h * 60
    return "${if (offset < 0) "-" else "+"}${h.absoluteValue.asTwo()}:${m.absoluteValue.asTwo()}"
}

internal fun asString(calc: Calendar, timeZone: String): String {
    val month = when (calc.month) {
        0 -> "Jan"
        1 -> "Feb"
        2 -> "Mar"
        3 -> "Apr"
        4 -> "May"
        5 -> "Jun"
        6 -> "Jul"
        7 -> "Aug"
        8 -> "Sep"
        9 -> "Oct"
        10 -> "Nov"
        11 -> "Dec"
        else -> "Unknown"
    }
    val week = when (calc.dayOfWeek) {
        0 -> "Sun"
        1 -> "Mon"
        2 -> "Tue"
        3 -> "Wed"
        4 -> "Thu"
        5 -> "Fri"
        6 -> "Sat"
        else -> "Unknown (${calc.dayOfWeek})"
    }
    return "$week, ${calc.dayOfMonth.asTwo()} $month ${calc.year} ${calc.hours.asTwo()}:${calc.minutes.asTwo()}:${calc.seconds.asTwo()} GMT$timeZone"
}

private fun Int.asTwo(): String =
    if (this > 9)
        toString()
    else
        "0$this"