package pw.binom.pool

import platform.posix.*

internal actual object ThreadUtils {
    actual val currentThreadId: Long = pthread_self()!!.rawValue.toLong()
}
