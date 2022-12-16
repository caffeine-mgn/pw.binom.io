package pw.binom.pool

import platform.windows.GetCurrentThreadId

internal actual object ThreadUtils {
    actual val currentThreadId: Long = GetCurrentThreadId().toLong()
}
