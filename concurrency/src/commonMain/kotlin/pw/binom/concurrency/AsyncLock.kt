package pw.binom.concurrency

interface AsyncLock {
    suspend fun <T> synchronize(func: suspend () -> T): T
}