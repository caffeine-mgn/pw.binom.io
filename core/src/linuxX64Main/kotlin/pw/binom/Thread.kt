package pw.binom

import kotlinx.cinterop.*
import platform.posix.CLOCK_REALTIME
import platform.posix.clock_gettime
import platform.posix.timespec
import platform.posix.usleep
import kotlin.math.round
import platform.posix.pthread_self

actual object Thread {
    actual fun sleep(time: Long) {
        usleep((time * 100).toUInt())
    }

    actual fun currentTimeMillis(): Long =
            memScoped {
                val spec = this.alloc<timespec>()
                clock_gettime(CLOCK_REALTIME, spec.ptr)

                var s = spec.tv_sec

                var ms = round(spec.tv_nsec / 1.0e6).toLong()
                if (ms > 999) {
                    s++
                    ms = 0
                }
                s * 1000 + ms
            }

    actual val id: Long
        get() = pthread_self().toLong()

}