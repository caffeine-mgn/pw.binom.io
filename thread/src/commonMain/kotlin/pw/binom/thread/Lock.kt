package pw.binom.thread

import pw.binom.atomic.AtomicInt
import pw.binom.atomic.AtomicLong
import pw.binom.doFreeze
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
        fun notify()
        fun notifyAll()
    }
}

class SpinLock {
    private val atom = AtomicLong(0)
    private val count = AtomicInt(0)

    fun lock() {
        if (atom.value == Worker.current?.id?:0)
            count.increment()
        else {
            while (true) {
                if (atom.compareAndSet(0, Worker.current?.id?:0))
                    break
                Worker.sleep(1)
            }
            count.increment()
        }
    }

    fun unlock() {
        if (atom.value == Worker.current?.id?:0)
            count.decrement()
        if (count.value == 0)
            if (!atom.compareAndSet(Worker.current?.id?:0, 0))
                throw IllegalStateException("Lock already free")
    }

    init {
        doFreeze()
    }
}

inline fun <T> SpinLock.use(func: () -> T): T =
        try {
            lock()
            func()
        } finally {
            unlock()
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