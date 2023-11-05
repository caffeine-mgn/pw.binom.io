package pw.binom.pool

import kotlinx.cinterop.UnsafeNumber
import platform.posix.pthread_self

@OptIn(UnsafeNumber::class)
internal actual object ThreadUtils {
  actual val currentThreadId: Long = pthread_self().toLong()
}
