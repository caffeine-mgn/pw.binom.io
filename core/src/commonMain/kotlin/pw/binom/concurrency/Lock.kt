package pw.binom.concurrency

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface Lock {
    /**
     * Trying lock. Infinity time
     */
    fun lock()
    fun tryLock(): Boolean
    fun unlock()
}

@OptIn(ExperimentalContracts::class)
inline fun <T> Lock.synchronize(func: () -> T): T {
    contract {
        callsInPlace(func, InvocationKind.EXACTLY_ONCE)
    }
    try {
        lock()
        return func()
    } finally {
        unlock()
    }
}

class LockTimeout : RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}
