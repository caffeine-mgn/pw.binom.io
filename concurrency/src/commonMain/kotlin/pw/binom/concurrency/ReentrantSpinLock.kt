package pw.binom.concurrency

import pw.binom.atomic.AtomicInt
import pw.binom.atomic.AtomicLong
import pw.binom.doFreeze
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class ReentrantSpinLock {
    private val threadId = AtomicLong(0)
    private val count = AtomicInt(0)

    override fun lock() {
        if (threadId.value == WorkerImpl.current?.id ?: 0)
            count.increment()
        else {
            while (true) {
                if (threadId.compareAndSet(0, WorkerImpl.current?.id ?: 0)) {
                    break
                }
                sleep(1)
            }
            count.increment()
        }
    }

    override fun unlock() {
        if (threadId.value != WorkerImpl.current?.id ?: 0) {
            throw IllegalStateException("Only locking thread can call unlock")
        }
        count.decrement()
        if (count.value == 0)
            if (!threadId.compareAndSet(WorkerImpl.current?.id ?: 0, 0))
                throw IllegalStateException("Lock already free")
    }

    init {
        doFreeze()
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> ReentrantSpinLock.synchronize(func: () -> T): T {
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