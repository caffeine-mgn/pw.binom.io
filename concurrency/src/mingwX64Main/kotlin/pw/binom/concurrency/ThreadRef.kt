package pw.binom.concurrency

import platform.windows.GetCurrentThreadId

internal actual val currentThreadId: Long
    get() = GetCurrentThreadId().toLong()