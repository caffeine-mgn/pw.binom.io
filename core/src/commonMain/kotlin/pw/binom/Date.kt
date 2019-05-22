package pw.binom

expect class Date {
    constructor(time: Long)
    internal constructor(year: Int, month: Int = 0, dayOfMonth: Int = 0, hours: Int = 0, min: Int = 0, sec: Int = 0)

    val year: Int
    val month: Int
    val dayOfMonth: Int
    val dayOfWeek:Int
    val hours: Int
    val min: Int
    val sec: Int
    val time: Long

    companion object {

        /**
         * Returns current time
         */
        fun now(): Date

        /**
         * Returns current offset UTC in minutes
         */
        val timeZoneOffset: Int
    }
}

fun Date.Companion.from(year: Int = 0, month: Int = 0, dayOfMonth: Int = 0, hours: Int = 0, min: Int = 0, sec: Int = 0) =
        Date(year, month, dayOfMonth, hours, min, sec)

fun Date.with(
        year: Int = this.year,
        month: Int = this.month,
        dayOfMonth: Int = this.dayOfMonth,
        hours: Int = this.hours,
        min: Int = this.min,
        sec: Int = this.sec
) = Date.from(year, month, dayOfMonth, hours, min, sec)