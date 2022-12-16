package pw.binom.network

import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.LockWithTimeout
import pw.binom.concurrency.sleep
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class MySpinLock(val name: String, private val lock: AtomicBoolean = AtomicBoolean(false)) : LockWithTimeout {
    override fun tryLock(): Boolean = lock.compareAndSet(expected = false, new = true)

    val isLocked
        get() = lock.getValue()

    override fun lock(timeout: Duration): Boolean {
        val now = TimeSource.Monotonic.markNow()
        var tries = 0
        while (true) {
            if (tryLock()) {
                break
            }
            tries++
            if (now.elapsedNow() > timeout) {
                println("MySpinLock::1:: $name Can't lock after ${now.elapsedNow()}")
                return false
            }
            sleep(1)
        }
        if (tries > 1) {
            println("MySpinLock::1:: $name Locked after ${now.elapsedNow()}")
        }
        return true
    }

    override fun lock() {
        var tries = 0
        val now = TimeSource.Monotonic.markNow()
        while (true) {
            if (tryLock()) {
                break
            }
            tries++
            sleep(1)
        }
        if (tries > 1) {
            println("MySpinLock::2:: $name Locked after ${now.elapsedNow()}")
        }
    }

    override fun unlock() {
        lock.setValue(false)
    }
}
