package pw.binom.concurrency

import platform.posix.*
import kotlinx.cinterop.memScoped

internal actual val currentThreadId: Long
    get() = pthread_self()!!.rawValue.toLong()