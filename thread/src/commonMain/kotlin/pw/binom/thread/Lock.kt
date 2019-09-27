package pw.binom.thread

import pw.binom.atomic.AtomicInt
import pw.binom.atomic.AtomicLong
import pw.binom.doFreeze

class Lock {
    private val atom = AtomicLong(0)
    private val count = AtomicInt(0)

    fun lock() {

        if (atom.value == Thread.currentThread.id)
            count.increment()
        else {
            while (true) {
                if (atom.compareAndSet(0, Thread.currentThread.id))
                    break
                Thread.sleep(1)
            }
            count.increment()
        }
    }

    fun unlock() {
        if (atom.value == Thread.currentThread.id)
            count.decrement()
        if (count.value == 0)
            if (!atom.compareAndSet(Thread.currentThread.id, 0))
                throw IllegalStateException("Lock already free")
    }

    init {
        doFreeze()
    }
}

inline fun <T> Lock.use(func: () -> T): T =
        try {
            lock()
            func()
        } finally {
            unlock()
        }