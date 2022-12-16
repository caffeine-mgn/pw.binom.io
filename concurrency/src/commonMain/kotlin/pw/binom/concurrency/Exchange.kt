package pw.binom.concurrency

import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@Suppress("UNCHECKED_CAST")
class Exchange<T> {
    private val lock = ReentrantLock()
    private var condition = lock.newCondition()
    private var valueExist = false
    private var value: T? = null

    @OptIn(ExperimentalTime::class)
    fun put(value: T) {
        val now = TimeSource.Monotonic.markNow()
        lock.synchronize {
            while (valueExist) {
                condition.await()
            }
            valueExist = true
            this.value = value
            condition.signalAll()
        }
    }

    @OptIn(ExperimentalTime::class)
    fun getOrNull(): T? {
        val now = TimeSource.Monotonic.markNow()
        return lock.synchronize {
            if (!valueExist) {
                null
            } else {
                valueExist = false
                val value = this.value
                this.value = null
                condition.signalAll()
                return@synchronize value as T
            }
        }
    }

    fun get(): T = lock.synchronize {
        while (!valueExist) {
            condition.await()
        }
        valueExist = false
        val value = this.value
        this.value = null
        condition.signalAll()
        return@synchronize value as T
    }
}
