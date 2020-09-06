package pw.binom.concurrency

internal actual val currentThreadId: Long
    get() = Thread.currentThread().id