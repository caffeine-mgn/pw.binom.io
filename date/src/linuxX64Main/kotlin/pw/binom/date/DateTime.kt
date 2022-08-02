@file:OptIn(UnsafeNumber::class)

package pw.binom.date

import kotlinx.cinterop.*
import platform.posix.*

actual value class DateTime(val time: Long = nowTime) {
    actual companion object {
        actual val systemZoneOffset: Int
            get() = memScoped {
                val t = alloc<time_tVar>()
                val t2 = alloc<tm>()
                localtime_r(t.ptr, t2.ptr)
                t2.tm_gmtoff.convert<Int>() / 60
            }
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
            timeZoneOffset: Int
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
}
