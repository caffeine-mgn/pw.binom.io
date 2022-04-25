package pw.binom.date

import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.*

actual value class Date(val time: Long = Date.nowTime) {
    actual companion object {
        actual val systemZoneOffset: Int
            get() = memScoped {
                val t = alloc<time_tVar>()
                val t2 = alloc<tm>()
                localtime_r(t.ptr, t2.ptr)
                t2.tm_gmtoff.convert<Int>() / 60

//                val t = alloc<timezone>()
//                val timeVal = alloc<timeval>()
//                gettimeofday(timeVal.ptr, t.ptr)
//                val r = -t.tz_minuteswest
//                r
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
        ): Date =
            memScoped {
                val t = alloc<tm>()
                t.tm_year = year - 1900
                t.tm_mon = month - 1
                t.tm_mday = dayOfMonth
                t.tm_hour = hours
                t.tm_min = minutes
                t.tm_sec = seconds
                val tx = timeZoneOffset - Date.systemZoneOffset
                val r = (mktime(t.ptr) - tx * 60L) * 1000L + millis
                Date(r)
            }

        actual val now: Date
            get() = Date(nowTime)
    }

    actual fun calendar(timeZoneOffset: Int): Calendar =
        Calendar(utcTime = time, offset = timeZoneOffset)
}