package pw.binom

/*
import kotlinx.cinterop.*
import platform.posix.*

internal actual fun nowTime(): Long = time(null)
internal actual fun calcTime(year: Int, month: Int, dayOfMonth: Int, hours: Int, min: Int, sec: Int): Long =
        memScoped {
            val t = alloc<tm>()
            t.tm_year = year
            t.tm_mon = month
            t.tm_mday = dayOfMonth
            t.tm_hour = hours
            t.tm_min = min
            t.tm_sec = sec
            mktime(t.ptr)
        }

actual typealias NTime = tm

internal actual fun getNativeTime(time: Long) = memScoped {
    val t = alloc<LongVar>()
    t.value = time
    localtime(t.ptr)!!.pointed
}

internal actual fun currentTimezoneOffset(): Int = memScoped {
    val t = alloc<timezone>()
    gettimeofday(null, t.ptr)
    t.tz_minuteswest
}*/
