package pw.binom.pool

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
internal actual object ThreadUtils {
    actual val currentThreadId: Long = pthread_self()!!.rawValue.toLong()
}
