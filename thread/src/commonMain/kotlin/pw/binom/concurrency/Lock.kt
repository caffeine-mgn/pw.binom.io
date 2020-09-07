package pw.binom.concurrency

import pw.binom.io.Closeable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

expect class Lock : Closeable {
    constructor()

    fun lock()
    fun unlock()

    fun newCondition(): Condition

    @OptIn(ExperimentalTime::class)
    class Condition : Closeable {
        fun wait()
        fun wait(duration: Duration): Boolean
        fun signal()
        fun signalAll()
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> Lock.synchronize(func: () -> T): T {
    contract {
        callsInPlace(func)
    }
    try {
        lock()
        return func()
    } finally {
        unlock()
    }
}