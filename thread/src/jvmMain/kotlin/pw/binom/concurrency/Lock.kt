package pw.binom.concurrency

import pw.binom.io.Closeable
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

actual class Lock : Closeable {

    private val native = ReentrantLock(false)

    actual fun lock() {
        native.lock()
    }

    actual fun unlock() {
        native.unlock()
    }

    override fun close() {
    }

    actual fun newCondition(): Condition =
            Condition(native, native.newCondition())

    actual class Condition(val lock: ReentrantLock, val native: java.util.concurrent.locks.Condition) : Closeable {
        @JvmName("wait5")
        actual fun wait() {
            native.await()
        }

        @JvmName("notify5")
        actual fun notify() {
            native.signal()
        }

        override fun close() {
        }

        @JvmName("notifyAll5")
        actual fun notifyAll() {
            native.signalAll()
        }

        @OptIn(ExperimentalTime::class)
        actual fun wait(duration: Duration): Boolean {
            return native.await(duration.toLongMilliseconds(), TimeUnit.MILLISECONDS)
        }
    }
}

internal inline fun <T> ReentrantLock.lock(func: () -> T): T =
        try {
            lock()
            func()
        } finally {
            unlock()
        }