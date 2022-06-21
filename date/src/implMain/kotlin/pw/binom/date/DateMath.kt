package pw.binom.date

internal object DateMath {
    const val MILLISECONDS_IN_SECOND = 1000L
    const val MILLISECONDS_IN_MINUTE = MILLISECONDS_IN_SECOND * 60
    const val MILLISECONDS_IN_HOUR = MILLISECONDS_IN_MINUTE * 60
    const val MILLISECONDS_IN_DAY = MILLISECONDS_IN_HOUR * 24
    const val DAYS_PER_CYCLE = 146097
    const val DAYS_0000_TO_1970 = DAYS_PER_CYCLE * 5 - (30 * 365 + 7)

    fun toEpochDay(year: Int, monthNumber: Int, dayOfMonth: Int): Int {
        val y = year
        val m = monthNumber
        var total = 0
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

    fun toMilisecodns(
        year: Int,
        monthNumber: Int,
        dayOfMonth: Int,
        hours: Int,
        minutes: Int,
        seconds: Int,
        milliseconds: Int
    ): Long {
        val days = toEpochDay(
            year = year,
            monthNumber = monthNumber,
            dayOfMonth = dayOfMonth,
        )
        val timeOfDay =
            hours * MILLISECONDS_IN_HOUR + minutes * MILLISECONDS_IN_MINUTE + seconds * MILLISECONDS_IN_SECOND + milliseconds
        return days * MILLISECONDS_IN_DAY + timeOfDay
    }

    fun isLeapYear(year: Int): Boolean {
        if (year % 400 == 0) return true
        if (year % 100 == 0) return false
        if (year % 4 == 0) return true
        return false
    }
}
