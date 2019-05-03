package pw.binom

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.GetSystemTime
import platform.windows.SYSTEMTIME
import platform.windows.Sleep
import platform.posix.pthread_self

actual object Thread {
    actual fun sleep(time: Long) {
        Sleep(time.toUInt())
    }

    /**
     * Returns current time in milliseconds
     *
     * @return current time in milliseconds
     */
    actual fun currentTimeMillis(): Long =
            memScoped {
                val time = alloc<SYSTEMTIME>()
                GetSystemTime(time.ptr);
                (time.wSecond * 1000u) + time.wMilliseconds
            }.toLong()

    /**
     * Returns current thread id
     */
    actual val id: Long
        get() = pthread_self().toLong()

}