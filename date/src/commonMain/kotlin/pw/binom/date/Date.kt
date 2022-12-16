package pw.binom.date

import kotlin.jvm.JvmInline

@JvmInline
value class Date(val epochDay: Long) {
    companion object {
        fun from(year: Int, monthNumber: Int, dayOfMonth: Int) =
            Date(toEpochDay(year = year, monthNumber = monthNumber, dayOfMonth = dayOfMonth))
    }

    val startOfDayMilliseconds
        get() = epochDay * MILLISECONDS_IN_DAY
}

// @JvmInline
// value class Date(val epochDay: Long) {
//    companion object {
//        fun of(dayOfMonth: Int, monthNumber: Int, year: Int) = Date(
//            toEpochDay(
//                year = year,
//                monthNumber = monthNumber,
//                dayOfMonth = dayOfMonth,
//            )
//        )
//    }
//
//    val inMiliseconds
//        get() = epochDay * MILLISECONDS_IN_DAY
// }
