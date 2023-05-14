package pw.binom.date

internal object DateMath {

    fun toMilisecodns(
        year: Int,
        monthNumber: Int,
        dayOfMonth: Int,
        hours: Int,
        minutes: Int,
        seconds: Int,
        milliseconds: Int,
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
}
