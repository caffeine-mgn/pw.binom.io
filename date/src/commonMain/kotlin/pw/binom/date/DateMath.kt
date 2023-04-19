package pw.binom.date

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

const val DAYS_PER_CYCLE = 146097
const val DAYS_0000_TO_1970 = DAYS_PER_CYCLE * 5 - (30 * 365 + 7)
const val MILLISECONDS_IN_SECOND = 1000L
const val MILLISECONDS_IN_MINUTE = MILLISECONDS_IN_SECOND * 60
const val MILLISECONDS_IN_HOUR = MILLISECONDS_IN_MINUTE * 60
const val MILLISECONDS_IN_DAY = MILLISECONDS_IN_HOUR * 24

internal fun toEpochDay(year: Int, monthNumber: Int, dayOfMonth: Int): Long {
    val y = year
    val m = monthNumber
    var total = 0L
    total += 365 * y
    if (y >= 0) {
        total += (y + 3) / 4 - (y + 99) / 100 + (y + 399) / 400
    } else {
        total -= y / -4 - y / -100 + y / -400
    }
    total += ((367 * m - 362) / 12)
    total += dayOfMonth - 1
    if (m > 2) {
        total--
        if (!isLeapYear(year)) {
            total--
        }
    }
    return total - DAYS_0000_TO_1970
}

private fun isLeapYear(year: Int): Boolean {
    if (year % 400 == 0) return true
    if (year % 100 == 0) return false
    return year % 4 == 0
}

internal fun dateTimeCompareTo(first: DateTime, second: DateTime) = when {
    first.time > second.time -> 1
    first.time < second.time -> -1
    else -> 0
}

internal fun dateTimePlus(date: DateTime, duration: Duration) =
    DateTime(date.time + duration.inWholeMilliseconds)

internal fun dateTimeMinus(date: DateTime, duration: Duration) =
    DateTime(date.time - duration.inWholeMilliseconds)

internal fun dateTimeMinus(date: DateTime, other: DateTime): Duration = (date.time - other.time).milliseconds
