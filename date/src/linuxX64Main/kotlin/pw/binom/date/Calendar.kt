package pw.binom.date

import kotlinx.cinterop.*
import platform.posix.localtime
import platform.posix.time_tVar

actual class Calendar(private val utcTime: Long, timeZoneOffset: Int) {

    private val tt = memScoped {
        val t = alloc<time_tVar>()
        val tx = timeZoneOffset - Date.timeZoneOffset
        t.value = (utcTime / 1000L + tx * 60L).convert()
        localtime(t.ptr)!!.pointed
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

    actual val dayOfWeek: Int
        get() = tt.tm_wday

    actual val date
        get() = Date(utcTime)

    actual fun timeZone(timeZoneOffset: Int): Calendar = Calendar(utcTime, timeZoneOffset)

    actual override fun toString(): String = timeZone(0).asStringGmt()
}