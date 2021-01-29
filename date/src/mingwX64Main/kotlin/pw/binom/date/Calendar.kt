package pw.binom.date

import kotlinx.cinterop.*
import platform.posix._localtime64

actual class Calendar(private val utcTime: Long, actual val timeZoneOffset: Int) {

    private val tt = memScoped {
        val t = alloc<LongVar>()
        val tx = timeZoneOffset - Date.timeZoneOffset
        t.value = utcTime / 1000L + tx * 60L
        _localtime64(t.ptr)!!.pointed
    }

    actual val year
        get() = tt.tm_year + 1900

    actual val month
        get() = tt.tm_mon

    actual val dayOfMonth
        get() = tt.tm_mday

    actual val minutes
        get() = tt.tm_min

    actual val millisecond
        get() = (utcTime - utcTime / 1000L * 1000L).toInt()

    actual val hours
        get() = tt.tm_hour

    actual val seconds
        get() = tt.tm_sec

    actual val date
        get() = Date(utcTime)

    actual val dayOfWeek: Int
        get() = tt.tm_wday

    actual fun timeZone(timeZoneOffset: Int): Calendar = Calendar(utcTime, timeZoneOffset)

    actual override fun toString(): String =
        asString(this, timeZoneOffsetToString(timeZoneOffset))

    actual fun toString(timeZoneOffset: Int): String = timeZone(timeZoneOffset).toString()
}