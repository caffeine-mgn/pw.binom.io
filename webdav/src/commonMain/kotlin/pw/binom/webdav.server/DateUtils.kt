package pw.binom.webdav.server

import pw.binom.date.*


fun Date.toUTC() = Date(time - Date.timeZoneOffset.toLong() * 60 * 1000)

fun String.parseDate(): Date {
    //Sat, 30 May 2020 02:05:17 GMT
    val p = indexOf(", ")
    var dayOfMonth = 0
    var month = 0
    var year = 0
    var h = 0
    var m = 0
    var ss = 0
    substring(p + 2)
            .splitToSequence(' ')
            .flatMap { it.splitToSequence(':') }
            .forEachIndexed { index, s ->
                when (index) {
                    0 -> dayOfMonth = s.toInt()
                    1 -> month = when (s) {
                        "Jan" -> 0
                        "Feb" -> 1
                        "Mar" -> 2
                        "Apr" -> 3
                        "May" -> 4
                        "Jun" -> 5
                        "Jul" -> 6
                        "Aug" -> 7
                        "Sep" -> 8
                        "Oct" -> 9
                        "Nov" -> 10
                        "Dec" -> 11
                        else -> TODO("Unknown \"$s\"")
                    }
                    2 -> year = s.toInt() - 1900
                    3 -> h = s.toInt()
                    4 -> m = s.toInt()
                    5 -> ss = s.toInt()
                }
            }
    val r = Date.of(
            year = year,
            dayOfMonth = dayOfMonth,
            hours = h,
            minutes = m,
            month = month,
            seconds = ss,
            timeZoneOffset = 0,
            millis = 0
    )
    return r
}

fun Date.asString(): String = calendar(0).asString()

fun Calendar.asString(): String {
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

    return "$week, ${dayOfMonth.asTwo()} $month ${year + 1900} ${hours.asTwo()}:${minutes.asTwo()}:${seconds.asTwo()} GMT"
}

private fun Int.asTwo(): String =
        if (this > 9)
            toString()
        else
            "0$this"