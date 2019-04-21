package pw.binom

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.GetSystemTime
import platform.windows.SYSTEMTIME
import platform.windows.Sleep

actual fun sleep(time: Long) {
    Sleep(time.toUInt())
}

actual fun currentTimeMillis(): Long =
        memScoped {
            val time = alloc<SYSTEMTIME>()
            GetSystemTime(time.ptr);
            (time.wSecond * 1000u) + time.wMilliseconds
        }.toLong()