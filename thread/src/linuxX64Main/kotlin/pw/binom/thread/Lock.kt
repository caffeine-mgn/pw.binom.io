package pw.binom.thread

import kotlinx.cinterop.*
import platform.posix.*
import pw.binom.atomic.AtomicInt
import pw.binom.io.Closeable
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private const val checkTime = 100

actual class Lock : Closeable {

    private val native = cValue<pthread_mutex_t>()//nativeHeap.alloc<pthread_mutex_t>()//.malloc(sizeOf<pthread_mutex_t>().convert())!!.reinterpret<pthread_mutex_t>()
    private var closed = AtomicInt(0)

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
        if (closed.value == 1)
            throw IllegalStateException("Lock already closed")
        pthread_mutex_destroy(native)
        closed.value = 1
    }

    actual fun newCondition(): Lock.Condition = Condition(native)

    actual class Condition(val mutex: CValue<pthread_mutex_t>) : Closeable {
        val native = cValue<pthread_cond_t>()//nativeHeap.alloc<pthread_cond_t>()//malloc(sizeOf<pthread_cond_t>().convert())!!.reinterpret<pthread_cond_t>()

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
                waitUntil.tv_nsec = (checkTime * 1000000L).convert()

                val now = TimeSource.Monotonic.markNow()
                while (true) {
                    val r = pthread_cond_timedwait(native.ptr, mutex.ptr, waitUntil.ptr)
                    if (Worker.current?.isInterrupted == true)
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