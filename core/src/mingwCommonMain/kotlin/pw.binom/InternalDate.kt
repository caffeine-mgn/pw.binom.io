package pw.binom

/*
import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.GetLocalTime
import platform.windows.GetSystemTime
import platform.windows.*
import platform.windows.LPSYSTEMTIMEVar

internal actual fun calcTime(year: Int, month: Int, dayOfMonth: Int, hours: Int, min: Int, sec: Int): Long =
        memScoped {
            val t = alloc<tm>()
            t.tm_year = year
            t.tm_mon = month
            t.tm_mday = dayOfMonth
            t.tm_hour = hours
            t.tm_min = min
            t.tm_sec = sec
            val r = _mktime64(t.ptr)
            r
        }

internal actual fun nowTime(): Long = _time64(null)
internal actual fun getNativeTime(time: Long) = memScoped {
    val t = alloc<LongVar>()
    val v = alloc<LPSYSTEMTIMEVar>()
    GetLocalTime(v.value)
    v.pointed!!.wMilliseconds
    t.value = time
    _localtime64(t.ptr)!!.pointed
}
actual typealias NTime = tm

internal actual fun currentTimezoneOffset(): Int = memScoped {
    val t = alloc<timezone>()
    mingw_gettimeofday(null, t.ptr)
    val r= t.tz_minuteswest
    r
}*/
