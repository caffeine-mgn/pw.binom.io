package pw.binom.concurrency

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.atomic.AtomicReference
import pw.binom.threadYield
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

// @JvmInline
@OptIn(ExperimentalTime::class)
class SpinLock(private val name: String? = null, private val lock: AtomicBoolean = AtomicBoolean(false)) :
    LockWithTimeout {
    private val currentLockName = AtomicReference<String?>(null)
    private val nowWaits = AtomicInt(0)
    private val id
        get() = hashCode().toUInt().toString(16)

    override fun tryLock(name: String?): Boolean = if (lock.compareAndSet(expected = false, new = true)) {
        println("SpinLock:: ${this.name} $id locked by $name")
        currentLockName.setValue(name)
        true
    } else {
        false
    }

    val isLocked
        get() = lock.getValue()

    override fun lock(timeout: Duration): Boolean {
        val now = TimeSource.Monotonic.markNow()
        while (true) {
            if (tryLock("timeout")) {
                break
            }
            if (now.elapsedNow() > timeout) {
                return false
            }
            threadYield()
        }
        return true
    }

    override fun lock(name: String?) {
        nowWaits.inc()
        while (true) {
            if (tryLock(name = name)) {
                nowWaits.dec()
                break
            }
            println("SpinLock:: ${this.name} $id. Can't lock :( wait of $name. Locked by ${this.currentLockName.getValue()}. now waits: ${nowWaits.getValue()}....")
//            threadYield()
        }
    }

    override fun unlock() {
        println("SpinLock:: ${this.name} $id unlocked by ${currentLockName.getValue()}")
        currentLockName.setValue(null)
        lock.setValue(false)
    }
}
