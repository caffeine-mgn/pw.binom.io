package pw.binom.concurrency

import pw.binom.atomic.AtomicBoolean
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.jvm.JvmInline
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@JvmInline
@OptIn(ExperimentalTime::class)
value class SpinLock(private val lock: AtomicBoolean = AtomicBoolean(false)) : Lock {
    fun tryLock(): Boolean = lock.compareAndSet(expected = false, new = true)

    val isLocked
        get() = lock.getValue()

    /**
     * Trying lock. If [duration] is not null will wait only [duration] time. And if [duration]==null will
     * wait until lock is free infinity time
     */
    fun lock(duration: Duration?): Boolean {
        val now = if (duration != null) TimeSource.Monotonic.markNow() else null
        while (true) {
            if (lock.compareAndSet(expected = false, new = true)) {
                break
            }
            if (now != null && now.elapsedNow() > duration!!) {
                return false
            }
            sleep(1)
        }
        return true
    }

    override fun lock() {
        lock(null)
    }

    override fun unlock() {
        lock.setValue(false)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> SpinLock.synchronize(duration: Duration, func: () -> T): T {
    contract {
        callsInPlace(func)
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
