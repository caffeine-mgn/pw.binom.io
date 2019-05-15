package pw.binom

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.math.round

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

fun timespec.toMillis():Long{
    var s = tv_sec

    var ms = round(tv_nsec / 1.0e6).toLong()
    if (ms > 999) {
        s++
        ms = 0
    }
    return s * 1000 + ms
}