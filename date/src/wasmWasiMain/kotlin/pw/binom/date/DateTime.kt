package pw.binom.date

import kotlin.time.Duration
import kotlin.time.TimeSource

actual value class DateTime(val milliseconds: Long) {
  actual companion object {
    private val markNow = TimeSource.Monotonic.markNow()
    actual val systemZoneOffset: Int
      get() = 0
    actual val nowTime: Long
      get() = markNow.elapsedNow().inWholeMilliseconds

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
      val date = DateMath.toMilisecodns(
        year=year,
        monthNumber = month - 1,
        dayOfMonth =dayOfMonth,
        hours = hours,
        minutes = minutes,
        seconds = seconds,
        milliseconds = millis,
      )
      val utc = date - timeZoneOffset * 60 * 1000
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
}
