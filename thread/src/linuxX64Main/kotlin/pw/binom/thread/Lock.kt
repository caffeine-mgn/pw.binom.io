package pw.binom.thread

import kotlinx.cinterop.*
import platform.posix.*
import pw.binom.io.Closeable
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private const val checkTime = 100

actual class Lock : Closeable {

    private val native = malloc(sizeOf<pthread_mutex_t>().convert())!!.reinterpret<pthread_mutex_t>()

    init {
        pthread_mutex_init(native, null)
    }

    actual fun lock() {
        pthread_mutex_lock(native)
    }

    actual fun unlock() {
        pthread_mutex_unlock(native)
    }

    override fun close() {
        pthread_mutex_destroy(native)
        free(native)
    }

    actual fun newCondition(): Lock.Condition = Condition(native)

    actual class Condition(val mutex: CPointer<pthread_mutex_t>) : Closeable {
        val native = malloc(sizeOf<pthread_cond_t>().convert())!!.reinterpret<pthread_cond_t>()

        init {
            pthread_cond_init(native, null)
        }

        actual fun wait() {
            pthread_cond_wait(native, mutex)
        }

        actual fun notify() {
            pthread_cond_signal(native)
        }

        override fun close() {
            notifyAll()
            pthread_cond_destroy(native)
        }

        actual fun notifyAll() {
            pthread_cond_broadcast(native)
        }

        @OptIn(ExperimentalTime::class)
        actual fun wait(duration: Duration): Boolean {
            if (duration.isInfinite()) {
                wait()
                return true
            }
            return memScoped<Boolean> {
                val waitUntil = alloc<timespec>()
                clock_gettime(CLOCK_REALTIME, waitUntil.ptr)
                waitUntil.tv_nsec = checkTime * 1000000L

                val now = TimeSource.Monotonic.markNow()
                while (true) {
                    val r = pthread_cond_timedwait(native, mutex, waitUntil.ptr)
                    if (Thread.currentThread.isInterrupted)
                        throw InterruptedException()
                    if (r == ETIMEDOUT) {
                        if (now.elapsedNow() > duration)
                            return@memScoped false
                        else
                            continue
                    }
                    if (r == 0)
                        return@memScoped true
                    throw TODO()
                }
                false
            }
        }
    }
}