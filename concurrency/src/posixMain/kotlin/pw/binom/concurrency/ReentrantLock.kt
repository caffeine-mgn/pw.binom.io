package pw.binom.concurrency

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.native.concurrent.AtomicNativePtr
import kotlin.native.concurrent.freeze
import kotlin.native.internal.createCleaner
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds

private inline val AtomicNativePtr.cc
    get() = this.value.reinterpret<pthread_cond_t>()!!

private inline val AtomicNativePtr.mm
    get() = this.value.reinterpret<pthread_mutex_t>()!!

private const val checkTime = 100
fun <T : NativePointed> NativePtr.reinterpret() = interpretNullablePointed<T>(this)

@OptIn(ExperimentalStdlibApi::class)
actual class ReentrantLock : Lock {

    private val native = nativeHeap.alloc<pthread_mutex_t>()

    init {
        pthread_mutex_init(native.ptr, null)
    }

    private val cleaner = createCleaner(native) { native ->
        pthread_mutex_destroy(native.ptr)
        nativeHeap.free(native)
    }

    init {
        freeze()
    }

    actual override fun lock() {
        if (pthread_mutex_lock(native.ptr) != 0) {
            throw IllegalStateException("Can't lock mutex")
        }
    }

    actual override fun unlock() {
        if (pthread_mutex_unlock(native.ptr) != 0) {
            throw IllegalStateException("Can't unlock mutex")
        }
    }

    actual fun newCondition() = Condition(native)

    actual class Condition(val mutex: pthread_mutex_t) {
        //        val native = cValue<pthread_cond_t>()//nativeHeap.alloc<pthread_cond_t>()//malloc(sizeOf<pthread_cond_t>().convert())!!.reinterpret<pthread_cond_t>()
        val native =
            nativeHeap.alloc<pthread_cond_t>() // malloc(sizeOf<pthread_cond_t>().convert())!!.reinterpret<pthread_cond_t>()

        init {
            if (pthread_cond_init(native.ptr, null) != 0) {
                throw IllegalStateException("Can't init Condition")
            }
        }

        private val cleaner = createCleaner(native) { native ->
            pthread_cond_destroy(native.ptr)
            nativeHeap.free(native)
        }

        actual fun await() {
            pthread_cond_wait(native.ptr, mutex.ptr)
        }

        actual fun signal() {
            pthread_cond_signal(native.ptr)
        }

        actual fun signalAll() {
            pthread_cond_broadcast(native.ptr)
        }

        actual fun await(duration: Duration): Boolean {
            if (duration.isInfinite()) {
                await()
                return true
            }
            return memScoped<Boolean> {
                val now = alloc<timeval>()
                val waitUntil = alloc<timespec>()
                gettimeofday(now.ptr, null)
                waitUntil.set(now, duration)
                while (true) {
                    val r = pthread_cond_timedwait(native.ptr, mutex.ptr, waitUntil.ptr)
                    if (Worker.current?.isInterrupted == true) {
                        throw InterruptedException()
                    }
                    if (r == ETIMEDOUT) {
                        return@memScoped false
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

private fun timespec.set(base: timeval, diff: Duration) {
    val nsecDiff = base.tv_usec.microseconds + diff

    tv_sec = base.tv_sec + nsecDiff.inWholeSeconds.toInt()
    tv_nsec = (nsecDiff.inWholeNanoseconds - nsecDiff.inWholeSeconds * 1_000_000_000).convert()
}
