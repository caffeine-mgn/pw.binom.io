package pw.binom.concurrency

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
internal actual val currentThreadId: Long
    get() = pthread_self()!!.rawValue.toLong()
