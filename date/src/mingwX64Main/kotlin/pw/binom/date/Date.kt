package pw.binom.date

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.*
import pw.binom.date.format.as2
import pw.binom.date.format.as3
import pw.binom.date.format.as4
import kotlin.native.concurrent.SharedImmutable

actual value class Date(val time: Long = nowTime) {
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
            timeZoneOffset: Int
        ): Date {
            val year2 = year - 1900
            val month2 = month - 1
            if (year <= 1970) {
                var yearDay = dayOfMonth
                var currentMonth = 0
                while (currentMonth < month2) {
                    var daysInCurrentMonth = daysInMonth[currentMonth]
                    if (currentMonth == 1 && isLeapYear(year)) {
                        daysInCurrentMonth++
                    }
                    yearDay += daysInCurrentMonth
                    currentMonth++
                }

                val time = seconds + minutes * 60L + hours * 3600L + (yearDay - 2) * 86400L +
                    (year2 - 70L) * 31536000L + ((year2 - 69L) / 4L) * 86400L -
                    ((year2 - 1L) / 100) * 86400L + ((year2 + 299L) / 400L) * 86400L
                return Date(time * 1000L)
            }
            return memScoped {

                val today_t = alloc<time_tVar>()
                time(today_t.ptr)

                val t = alloc<tm>()

                t.tm_year = year2
                t.tm_mon = month2
                t.tm_mday = dayOfMonth
                t.tm_hour = hours
                t.tm_min = minutes
                t.tm_sec = seconds
                val tx = timeZoneOffset - Date.systemZoneOffset
                println("Date.of tx=$tx")
                val time = _mktime64(t.ptr)
                if (time == -1L) {
                    throw IllegalStateException("Fail on _mktime64. Input: ${year.as4()}-${month.as2()}-${dayOfMonth.as2()} ${hours.as2()}:${minutes.as2()}:${seconds.as2()}.${millis.as3()} TZ=$timeZoneOffset")
                }
                val r = (time - tx * 60L) * 1000L + millis
                Date(r)
            }
        }

        actual val now: Date
            get() = Date(nowTime)
    }

    actual fun calendar(timeZoneOffset: Int): Calendar =
        Calendar(utcTime = time, offset = timeZoneOffset)
}

@SharedImmutable
private val daysInMonth = arrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

private fun isLeapYear(year: Int): Boolean {
    if (year % 400 == 0) return true
    if (year % 100 == 0) return false
    if (year % 4 == 0) return true
    return false
}
