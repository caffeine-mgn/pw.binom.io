package pw.binom.concurrency

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.time.Duration
import java.lang.InterruptedException as JInterruptedException

actual class ReentrantLock : Lock {

    private val native = ReentrantLock(false)

    override fun lock() {
        native.lock()
    }

    override fun tryLock(): Boolean = native.tryLock()

    override fun unlock() {
        native.unlock()
    }

    actual fun newCondition(): Condition = Condition(native, native.newCondition())

    actual class Condition(val lock: ReentrantLock, val native: java.util.concurrent.locks.Condition) {
        @JvmName("wait5")
        actual fun await() {
            try {
                native.await()
            } catch (e: JInterruptedException) {
                throw InterruptedException()
            }
        }

        actual fun signal() {
            native.signal()
        }

        actual fun signalAll() {
            native.signalAll()
        }

        actual fun await(duration: Duration): Boolean {
            try {
                return native.await(duration.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            } catch (e: JInterruptedException) {
                throw InterruptedException()
            }
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
