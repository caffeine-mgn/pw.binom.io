package pw.binom.concurrency

import kotlin.time.Duration

interface AsyncLock {
    val isLocked: Boolean
    suspend fun <T> synchronize(lockingTimeout: Duration, func: suspend () -> T): T
    suspend fun <T> synchronize(func: suspend () -> T): T
}
