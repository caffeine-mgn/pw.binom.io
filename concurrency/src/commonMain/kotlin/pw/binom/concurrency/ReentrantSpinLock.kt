package pw.binom.concurrency

import pw.binom.atomic.AtomicInt
import pw.binom.atomic.AtomicLong
import pw.binom.doFreeze
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class ReentrantSpinLock : Lock {
    private val threadId = AtomicLong(0)
    private val count = AtomicInt(0)

    override fun lock() {
        if (threadId.getValue() == (Worker.current?.id ?: 0)) {
            count.increment()
        } else {
            while (true) {
                if (threadId.compareAndSet(0, Worker.current?.id ?: 0)) {
                    break
                }
                sleep(1)
            }
            count.increment()
        }
    }

    fun lock(duration: Duration?): Boolean {
        if (threadId.getValue() == (Worker.current?.id ?: 0)) {
            count.increment()
            return true
        }
        val now = if (duration != null) TimeSource.Monotonic.markNow() else null
        while (true) {
            if (threadId.compareAndSet(0, Worker.current?.id ?: 0)) {
                break
            }
            if (now != null && now.elapsedNow() > duration!!) {
                return false
            }
            sleep(1)
        }
        return true
    }

    override fun unlock() {
        if (count.getValue() <= 0) {
            throw IllegalStateException("ReentrantSpinLock is not locked")
        }
        if (threadId.getValue() != (Worker.current?.id ?: 0)) {
            throw IllegalStateException("Only locking thread can call unlock")
        }
        count.decrement()
        if (count.getValue() == 0) {
            if (!threadId.compareAndSet(Worker.current?.id ?: 0, 0)) {
                throw IllegalStateException("Lock already free")
            }
        }
    }

    init {
        doFreeze()
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> ReentrantSpinLock.synchronize(duration: Duration, func: () -> T): T {
    contract {
        callsInPlace(func, InvocationKind.AT_MOST_ONCE)
    }
    try {
        if (!lock(duration)) {
            throw LockTimeout("Can't lock SpinLock with duration $duration")
        }
        return func()
    } finally {
        unlock()
    }
}
