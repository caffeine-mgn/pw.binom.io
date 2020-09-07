package pw.binom.concurrency

import platform.posix.pthread_self

internal actual val currentThreadId: Long
    get() = pthread_self().toLong()