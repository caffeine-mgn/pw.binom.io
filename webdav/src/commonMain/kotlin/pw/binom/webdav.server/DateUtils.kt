package pw.binom.webdav.server

import pw.binom.Date

fun Date.toUTC() = Date(time - Date.timeZoneOffset.toLong() * 60 * 1000)

fun Date.asString(): String {
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
        else -> "Unknown"
    }

    return "$week, ${dayOfMonth.asTwo()} $month ${year + 1900} ${hours.asTwo()}:${min.asTwo()}:${sec.asTwo()} GMT"
}

private fun Int.asTwo(): String =
        if (this > 9)
            toString()
        else
            "0$this"