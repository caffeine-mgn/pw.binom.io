package pw.binom.concurrency

import platform.posix.*

internal actual val currentThreadId: Long
    get() = pthread_self()!!.rawValue.toLong()
