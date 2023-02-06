package pw.binom.concurrency

import pw.binom.atomic.AtomicBoolean
import kotlin.jvm.JvmInline
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@JvmInline
@OptIn(ExperimentalTime::class)
value class SpinLock(private val lock: AtomicBoolean = AtomicBoolean(false)) : LockWithTimeout {
    override fun tryLock(): Boolean = lock.compareAndSet(expected = false, new = true)

    val isLocked
        get() = lock.getValue()

    override fun lock(timeout: Duration): Boolean {
        val now = TimeSource.Monotonic.markNow()
        while (true) {
            if (tryLock()) {
                break
            }
            if (now.elapsedNow() > timeout) {
                return false
            }
        }
        return true
    }

    override fun lock() {
        while (true) {
            if (tryLock()) {
                break
            }
        }
    }

    override fun unlock() {
        lock.setValue(false)
    }
}
