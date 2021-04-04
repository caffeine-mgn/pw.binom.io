package pw.binom.concurrency

import pw.binom.atomic.AtomicBoolean
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

inline class SpinLock(private val lock: AtomicBoolean = AtomicBoolean(false)) {
    fun lock() {
        while (true) {
            if (lock.compareAndSet(expected = false, new = true)) {
                break
            }
            Worker.sleep(1)
        }
    }

    fun unlock() {
        lock.value = false
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> SpinLock.synchronize(func: () -> T): T {
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