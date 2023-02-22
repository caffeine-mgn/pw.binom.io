package pw.binom.concurrency

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration

@OptIn(ExperimentalContracts::class)
inline fun <T> Lock.synchronize(name: String? = null, func: () -> T): T {
    contract {
        callsInPlace(func, InvocationKind.EXACTLY_ONCE)
    }
    try {
        lock(name = name)
        return func()
    } finally {
        unlock()
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> LockWithTimeout.synchronize(timeout: Duration, func: () -> T): T {
    contract {
        callsInPlace(func, InvocationKind.AT_MOST_ONCE)
    }
    try {
        lock(timeout)
        return func()
    } finally {
        unlock()
    }
}
