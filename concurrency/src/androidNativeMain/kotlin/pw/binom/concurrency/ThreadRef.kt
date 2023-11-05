package pw.binom.concurrency

import kotlinx.cinterop.UnsafeNumber
import platform.posix.pthread_self

@OptIn(UnsafeNumber::class)
internal actual val currentThreadId: Long
  get() = pthread_self().toLong()
