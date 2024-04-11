package pw.binom.date

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import kotlin.time.Duration

@JvmInline
actual value class DateTime(val milliseconds: Long) {
  actual companion object {
    actual val systemZoneOffset: Int
      get() = TimeZone.getDefault().rawOffset / 1000 / 60

    actual val nowTime: Long
      get() = System.currentTimeMillis()

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
      return DateTime(
        ZonedDateTime.of(
          year,
          month,
          dayOfMonth,
          hours,
          minutes,
          seconds,
          millis * 1_000_000,
          ZoneOffset.ofHoursMinutes(timeZoneOffset / 60, timeZoneOffset - (timeZoneOffset / 60 * 60)),
        ).toInstant().toEpochMilli(),
      )
    }

    actual val now: DateTime
      get() = DateTime(nowTime)
  }

  actual fun calendar(timeZoneOffset: Int): Calendar = Calendar(utcTime = milliseconds, timeZoneOffset = timeZoneOffset)

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
