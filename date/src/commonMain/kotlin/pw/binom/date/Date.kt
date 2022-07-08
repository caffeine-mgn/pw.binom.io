package pw.binom.date

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
