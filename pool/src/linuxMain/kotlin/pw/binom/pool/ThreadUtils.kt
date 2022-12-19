package pw.binom.pool

import platform.posix.pthread_self

internal actual object ThreadUtils {
    actual val currentThreadId: Long = pthread_self().toLong()
}
