@file:JvmName("LockUtilsKt")
package pw.binom.concurrency

import pw.binom.io.Closeable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

expect class Lock : Closeable {
    constructor()

    fun lock()
    fun unlock()

    fun newCondition(): Condition

    @OptIn(ExperimentalTime::class)
    class Condition : Closeable {
        fun await()

        /**
         * Stops the current thread until it receives an event. Event can send call of [signal] or [signalAll]
         *
         * @return [false] if the waiting time detectably elapsed before return from the method, else [true]
         */
        fun await(duration: Duration): Boolean
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