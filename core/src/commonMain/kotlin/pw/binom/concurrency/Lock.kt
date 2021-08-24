package pw.binom.concurrency

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

interface Lock {
    /**
     * Trying lock. Infinity time
     */
    fun lock()
    fun unlock()
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