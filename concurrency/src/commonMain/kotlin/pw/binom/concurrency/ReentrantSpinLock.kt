package pw.binom.concurrency

import pw.binom.atomic.AtomicInt
import pw.binom.atomic.AtomicLong
import pw.binom.doFreeze
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class ReentrantSpinLock : Lock {
    private val threadId = AtomicLong(0)
    private val count = AtomicInt(0)

    override fun lock() {
        if (threadId.value == (Worker.current?.id ?: 0))
            count.increment()
        else {
            while (true) {
                if (threadId.compareAndSet(0, Worker.current?.id ?: 0)) {
                    break
                }
                sleep(1)
            }
            count.increment()
        }
    }

    override fun unlock() {
        if (count.value <= 0) {
            throw IllegalStateException("ReentrantSpinLock is not locked")
        }
        if (threadId.value != (Worker.current?.id ?: 0)) {
            throw IllegalStateException("Only locking thread can call unlock")
        }
        count.decrement()
        if (count.value == 0) {
            if (!threadId.compareAndSet(Worker.current?.id ?: 0, 0)) {
                throw IllegalStateException("Lock already free")
            }
        }
    }

    init {
        doFreeze()
    }
}