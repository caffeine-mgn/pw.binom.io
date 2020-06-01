package pw.binom.date

expect inline class Date(val time: Long) {
    companion object {
        val timeZoneOffset: Int
        val now: Long

        /**
         * @param year full year. For example 2010
         */
        fun of(year: Int, month: Int, dayOfMonth: Int, hours: Int, minutes: Int, seconds: Int, millis: Int, timeZoneOffset: Int = 0):Date
    }

    fun calendar(timeZoneOffset: Int = Date.timeZoneOffset): Calendar
}