package pw.binom.concurrency

import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.GetSystemInfo
import platform.windows.SYSTEM_INFO
import platform.windows.Sleep

actual fun Worker.Companion.sleep(deley: Long) {
    platform.windows.Sleep(deley.toUInt())
}

actual val Worker.Companion.availableProcessors: Int
    get() =
        memScoped {
            val sysInfo = alloc<SYSTEM_INFO>()
            GetSystemInfo(sysInfo.ptr)
            sysInfo.dwNumberOfProcessors
        }.convert()