package pw.binom.date

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.*
import kotlin.time.Duration

actual value class DateTime(val time: Long) {
  @OptIn(ExperimentalForeignApi::class)
  actual companion object {
    actual val systemZoneOffset: Int
      get() = memScoped {
        val t = alloc<timezone>()
        val timeVal = alloc<timeval>()
        mingw_gettimeofday(timeVal.ptr, t.ptr)
        val r = -t.tz_minuteswest
        r
      }
    actual val nowTime: Long
      get() = memScoped {
        val ff = alloc<timespec>()
        clock_gettime(CLOCK_REALTIME, ff.ptr)
        ff.tv_sec * 1000L + ff.tv_nsec / 1000000L
      }

    actual fun internalOf(
      year: Int,
      month: Int,
      dayOfMonth: Int,
      hours: Int,
      minutes: Int,
      seconds: Int,
      millis: Int,
      timeZoneOffset: Int,
    ) = DateTime(
      DateMath.toMilisecodns(
        year = year,
        monthNumber = month,
        dayOfMonth = dayOfMonth,
        hours = hours,
        minutes = minutes,
        seconds = seconds,
        milliseconds = millis,
      ) - timeZoneOffset * MILLISECONDS_IN_MINUTE,
    )

    actual val now: DateTime
      get() = DateTime(nowTime)
  }

  actual fun calendar(timeZoneOffset: Int): Calendar =
    Calendar(utcTime = time, offset = timeZoneOffset)

  actual operator fun compareTo(expDate: DateTime): Int = dateTimeCompareTo(this, expDate)
  actual operator fun plus(duration: Duration) = dateTimePlus(date = this, duration = duration)
  actual operator fun minus(duration: Duration) = dateTimeMinus(date = this, duration = duration)
  actual operator fun minus(other: DateTime) = dateTimeMinus(
    date = this,
    other = other,
  )
}

private val daysInMonth = arrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
