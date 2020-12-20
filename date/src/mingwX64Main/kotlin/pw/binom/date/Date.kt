package pw.binom.date

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.*

actual inline class Date(val time: Long) {
    actual companion object {
        actual val timeZoneOffset: Int
            get() = memScoped {
                val t = alloc<timezone>()
                val timeVal = alloc<timeval>()
                mingw_gettimeofday(timeVal.ptr, t.ptr)
                val r = -t.tz_minuteswest
                r
            }
        actual val now: Long
            get() = memScoped {
                val ff = alloc<timespec>()
                clock_gettime(CLOCK_REALTIME, ff.ptr)
                ff.tv_sec * 1000L + ff.tv_nsec / 1000000L
            }

        actual fun internalOf(year: Int, month: Int, dayOfMonth: Int, hours: Int, minutes: Int, seconds: Int, millis: Int, timeZoneOffset: Int) =
                memScoped {
                    val t = alloc<tm>()
                    t.tm_year = year - 1900
                    t.tm_mon = month
                    t.tm_mday = dayOfMonth
                    t.tm_hour = hours
                    t.tm_min = minutes
                    t.tm_sec = seconds
                    val tx = timeZoneOffset - Date.timeZoneOffset
                    val r = (_mktime64(t.ptr) - tx * 60L) * 1000L
                    Date(r)
                }
    }

    actual fun calendar(timeZoneOffset: Int): Calendar =
            Calendar(time, timeZoneOffset)
}