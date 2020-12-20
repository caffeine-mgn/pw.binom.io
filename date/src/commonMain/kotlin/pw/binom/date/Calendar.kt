package pw.binom.date

import kotlin.jvm.JvmName

expect class Calendar {
    val year: Int
    val month: Int
    val dayOfMonth: Int
    val dayOfWeek: Int
    val hours: Int
    val minutes: Int
    val seconds: Int
    val millisecond: Int
    val date: Date
    override fun toString(): String

    fun timeZone(timeZoneOffset: Int): Calendar
}

internal fun Calendar.asStringGmt(): String {
    val month = when (month) {
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
    val week = when (dayOfWeek) {
        0 -> "Sun"
        1 -> "Mon"
        2 -> "Tue"
        3 -> "Wed"
        4 -> "Thu"
        5 -> "Fri"
        6 -> "Sat"
        else -> "Unknown ($dayOfWeek)"
    }
    return "$week, ${dayOfMonth.asTwo()} $month ${year} ${hours.asTwo()}:${minutes.asTwo()}:${seconds.asTwo()} GMT"
}

private fun Int.asTwo(): String =
        if (this > 9)
            toString()
        else
            "0$this"