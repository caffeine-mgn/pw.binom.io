package pw.binom.concurrency

import pw.binom.atomic.AtomicInt
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

inline class SimpleSpinLock(private val lock: AtomicInt = AtomicInt(0)) {
    fun lock() {
        while (true) {
            if (lock.compareAndSet(0, 1))
                break
            Worker.sleep(1)
        }
    }

    fun unlock() {
        lock.value = 0
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> SimpleSpinLock.synchronize(func: () -> T): T {
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