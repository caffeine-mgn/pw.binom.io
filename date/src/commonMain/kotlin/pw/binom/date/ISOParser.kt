package pw.binom.date

import pw.binom.date.format.toDatePattern

val dd1 = "yyyy-MM-dd".toDatePattern()
val dd2 = "yyyy-MM-dd HH:mm:ss".toDatePattern()
val dd3 = "yyyy-MM-dd HH:mm:ssXXX".toDatePattern()

fun String.parseIsoDate(defaultTimezoneOffset: Int = Date.timeZoneOffset): Date? {

    return dd1.parseOrNull(this, defaultTimezoneOffset)
        ?: dd2.parseOrNull(this, defaultTimezoneOffset)
        ?: dd3.parseOrNull(this, defaultTimezoneOffset)
//    when (this[7]) {
//        '-', '/', ':', '.' -> {
//        }
//        else -> return null
//    }
    var u = 0

    fun String.nextDate(): Boolean {
        if (u >= length) {
            return false
        }
        if (this[u] == '-' || this[u] == '.' || this[u] == '/') {
            u++
        }
        if (u >= length) {
            return false
        }
        return this[u] in '0'..'9'
    }

    var year = 0
    var month = 0
    var dayOfMonth = 0
    var hours = 0
    var minutes = 0
    var seconds = 0
    var millis = 0
    var timeZoneOffset = defaultTimezoneOffset

    fun makeDate() =
        Date.internalOf(
            year = year,
            month = month,
            dayOfMonth = dayOfMonth,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            millis = millis,
            timeZoneOffset = timeZoneOffset,
        )

    year = substring(u, u + 4).toIntOrNull() ?: return null
    u += 4
    if (!nextDate()) {
        return null
    }
    month = substring(u, u + 2).toIntOrNull() ?: return null
    u += 2
    if (!nextDate()) {
        return null
    }
    dayOfMonth = substring(u, u + 2).toIntOrNull() ?: return null
    u += 2
    if (length == u) {
        return makeDate()
    }
    if (this[u] != ' ' && this[u] != 'T') {
        return null
    }
    u++
    if (u + 1 >= length) {
        return null
    }
    hours = substring(u, u + 2).toIntOrNull() ?: return null
    u += 2
    if (u >= length) {
        return null
    }
    if (this[u] != ':') {
        return null
    }
    u++

    minutes = substring(u, u + 2).toIntOrNull() ?: return null
    u += 2
    if (u >= length) {
        return null
    }
    if (this[u] != ':') {
        return null
    }
    u++
    seconds = substring(u, u + 2).toIntOrNull() ?: return null
    u += 2
    if (u == length) {
        return makeDate()
    }

    if (u >= length) {
        return null
    }
    if (this[u] == '+' || this[u] == '-') {
        u++
        if (u >= length) {
            return null
        }
        if (this[u] in '0'..'9') {//timezone offset in HH:MM
            TODO()
        }
    }

    TODO("->${length}  u=$u")
}
