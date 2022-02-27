package pw.binom.concurrency

interface AsyncLock {
    val isLocked: Boolean
    suspend fun <T> synchronize(func: suspend () -> T): T
}
