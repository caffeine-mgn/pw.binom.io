package pw.binom.date

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.time.Duration

actual value class DateTime(val time: Long) {
  @OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
  actual companion object {
    actual val systemZoneOffset: Int
      get() {
        val t = nativeHeap.alloc<time_tVar>()
        val t2 = nativeHeap.alloc<tm>()
        try {
          memset(t.ptr, 0, sizeOf<time_tVar>().convert())
          val r = localtime_r(t.ptr, t2.ptr) ?: throw IllegalStateException("Can't get current time")
          return r.pointed.tm_gmtoff.convert<Int>() / 60
        } finally {
          nativeHeap.free(t)
          nativeHeap.free(t2)
        }
      }

    //            get() = memScoped {
//                val b = nativeHeap.alloc<tm>()
//                nativeHeap.free(b)
//                val t = cValue<time_tVar>()
//                val t2 = cValue<tm>()
//                localtime_r(t.ptr, t2.ptr)
//                val result = t2.useContents { tm_gmtoff.convert<Int>() } / 60
//                result
//            }
    actual val nowTime: Long
      get() = memScoped {
        val ff = alloc<timespec>()
        clock_gettime(CLOCK_REALTIME, ff.ptr)
        ff.tv_sec * 1000L + ff.tv_nsec / 1000000L
      }

    /**
     * @param year full year. For example 2010
     */
    actual fun internalOf(
      year: Int,
      month: Int,
      dayOfMonth: Int,
      hours: Int,
      minutes: Int,
      seconds: Int,
      millis: Int,
      timeZoneOffset: Int,
    ): DateTime =
      memScoped {
        val t = alloc<tm>()
        t.tm_year = year - 1900
        t.tm_mon = month - 1
        t.tm_mday = dayOfMonth
        t.tm_hour = hours
        t.tm_min = minutes
        t.tm_sec = seconds
        val tx = timeZoneOffset - systemZoneOffset
        val r = (mktime(t.ptr) - tx * 60L) * 1000L + millis
        DateTime(r)
      }

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
