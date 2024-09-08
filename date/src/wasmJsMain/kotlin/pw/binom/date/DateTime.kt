package pw.binom.date

import kotlin.time.Duration

actual value class DateTime(val milliseconds: Long) {
  actual companion object {
    actual val systemZoneOffset: Int
      get() = -JsDate().getTimezoneOffset().toInt()
    actual val nowTime: Long
      get() = JsDate.now().toDouble().toLong()

    actual fun internalOf(
      year: Int,
      month: Int,
      dayOfMonth: Int,
      hours: Int,
      minutes: Int,
      seconds: Int,
      millis: Int,
      timeZoneOffset: Int,
    ): DateTime {
      val date = JsDate.UTC(year.toJsNumber(), (month - 1).toJsNumber(), dayOfMonth.toJsNumber(), hours.toJsNumber(), minutes.toJsNumber(), seconds.toJsNumber(), millis.toJsNumber())
      val utc = date.toDouble().toLong() - timeZoneOffset * 60 * 1000
      return DateTime(utc)
    }

    actual val now: DateTime
      get() = DateTime(nowTime)
  }

  actual fun calendar(timeZoneOffset: Int): Calendar = Calendar(utcTime = milliseconds, offset = timeZoneOffset)

  actual operator fun compareTo(expDate: DateTime): Int = dateTimeCompareTo(this, expDate)

  actual operator fun plus(duration: Duration) = dateTimePlus(date = this, duration = duration)

  actual operator fun minus(duration: Duration) = dateTimeMinus(date = this, duration = duration)

  actual operator fun minus(other: DateTime) =
    dateTimeMinus(
      date = this,
      other = other,
    )

  override fun toString(): String = dateTimeToString(this)

  /**
   * Convert Binom date to JS date
   */
  val js
    get() = JsDate(milliseconds.toDouble().toJsNumber())
}

/**
 *  Convert JS date to Binom date
 */
val JsDate.binom
  get() = DateTime(getTime().toDouble().toLong())
//  get() = DateTime.internalOf(
//    year = getUTCFullYear(),
//    month = getUTCMonth(),
//    dayOfMonth = getUTCDate(),
//    hours = getUTCHours(),
//    minutes = getUTCMinutes(),
//    seconds = getUTCSeconds(),
//    millis = getUTCMilliseconds(),
//    timeZoneOffset = 0,
//  )
