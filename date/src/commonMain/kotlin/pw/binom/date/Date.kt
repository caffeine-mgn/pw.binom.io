package pw.binom.date

import kotlin.jvm.JvmInline
import kotlin.time.Duration

@JvmInline
value class Date(val epochDay: Long) {
  companion object {
    val now: Date
      get() = Date(DateTime.nowTime / MILLISECONDS_IN_DAY)

    fun of(
      year: Int,
      monthNumber: Int,
      dayOfMonth: Int,
    ) = Date(toEpochDay(year = year, monthNumber = monthNumber, dayOfMonth = dayOfMonth))

    fun ofMilliseconds(milliseconds: Long) = Date(milliseconds / MILLISECONDS_IN_DAY)

    fun fromIso8601(date: String): Date {
      val items = date.split('-', limit = 3)

      fun throwInvalidDate(): Nothing = throw IllegalArgumentException("Invalid date \"$date\"")
      if (items.size != 3) {
        throwInvalidDate()
      }
      return of(
        year = items[0].toIntOrNull() ?: throwInvalidDate(),
        monthNumber = items[1].toIntOrNull() ?: throwInvalidDate(),
        dayOfMonth = items[2].toIntOrNull() ?: throwInvalidDate(),
      )
    }
  }

  /**
   * Returns first DateTime of current date
   */
  val startOfDay
    get() = DateTime(epochDay * MILLISECONDS_IN_DAY)

  /**
   * returns last DateTime of current date
   */
  val endOfDay
    get() = DateTime(epochDay * MILLISECONDS_IN_DAY + (MILLISECONDS_IN_DAY - 1))

  fun withTime(time: Time) = DateTime(startOfDay.milliseconds + time.milliseconds)

  val year: Int
    get() {
      val year: Int
      extractDateItems(
        utc = startOfDay.milliseconds,
        offset = Duration.ZERO,
        year = { year = it },
      )
      return month
    }

  val month: Int
    get() {
      val month: Int
      extractDateItems(
        utc = startOfDay.milliseconds,
        offset = Duration.ZERO,
        month = { month = it },
      )
      return month
    }
  val dayOfMonth: Int
    get() {
      val dayOfMonth: Int
      extractDateItems(
        utc = startOfDay.milliseconds,
        offset = Duration.ZERO,
        dayOfMonth = { dayOfMonth = it },
      )
      return dayOfMonth
    }

  fun iso8601(): String {
    val year: Int
    val month: Int
    val dayOfMonth: Int
    extractDateItems(
      utc = startOfDay.milliseconds,
      offset = Duration.ZERO,
      year = { year = it },
      dayOfMonth = { dayOfMonth = it },
      month = { month = it },
    )
    return year.toString().padStart(4, '0') + "-" +
      month.toString().padStart(2, '0') + "-" +
      dayOfMonth.toString().padStart(2, '0')
  }

  override fun toString(): String = "Date(${iso8601()})"
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
