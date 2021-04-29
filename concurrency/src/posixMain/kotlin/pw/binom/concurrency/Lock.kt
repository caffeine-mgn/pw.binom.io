package pw.binom.concurrency

import kotlinx.cinterop.*
import platform.posix.*
import pw.binom.atomic.AtomicInt
import pw.binom.io.Closeable
import kotlin.native.concurrent.AtomicNativePtr
import kotlin.native.concurrent.freeze
import kotlin.native.internal.createCleaner
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private inline val AtomicNativePtr.cc
    get() = this.value.reinterpret<pthread_cond_t>()!!

private inline val AtomicNativePtr.mm
    get() = this.value.reinterpret<pthread_mutex_t>()!!

private const val checkTime = 100
fun <T : NativePointed> NativePtr.reinterpret() = interpretNullablePointed<T>(this)
@OptIn(ExperimentalStdlibApi::class)
actual class Lock {

    private val native = AtomicNativePtr(nativeHeap.alloc<pthread_mutex_t>().rawPtr)

    init {
        pthread_mutex_init(native.mm.ptr, null)
        createCleaner(native){native->
            pthread_mutex_destroy(native.mm.ptr)
            nativeHeap.free(native.value)
        }
        freeze()
    }

    actual fun lock() {
        if (pthread_mutex_lock(native.mm.ptr) != 0)
            throw IllegalStateException("Can't lock mutex")
    }

    actual fun unlock() {
        if (pthread_mutex_unlock(native.mm.ptr) != 0)
            throw IllegalStateException("Can't unlock mutex")
    }

    actual fun newCondition() = Condition(native)

    actual class Condition(val mutex: AtomicNativePtr) {
        //        val native = cValue<pthread_cond_t>()//nativeHeap.alloc<pthread_cond_t>()//malloc(sizeOf<pthread_cond_t>().convert())!!.reinterpret<pthread_cond_t>()
        val native =
            AtomicNativePtr(nativeHeap.alloc<pthread_cond_t>().rawPtr)//malloc(sizeOf<pthread_cond_t>().convert())!!.reinterpret<pthread_cond_t>()

        init {
            if (pthread_cond_init(native.cc.ptr, null) != 0)
                throw IllegalStateException("Can't init Condition")
            createCleaner(native) { native ->
                pthread_cond_destroy(native.cc.ptr)
                nativeHeap.free(native.value)
            }
            freeze()
        }

        actual fun await() {
            pthread_cond_wait(native.cc.ptr, mutex.mm.ptr)
        }

        actual fun signal() {
            pthread_cond_signal(native.cc.ptr)
        }

        actual fun signalAll() {
            pthread_cond_broadcast(native.cc.ptr)
        }

        @OptIn(ExperimentalTime::class)
        actual fun await(duration: Duration): Boolean {
            if (duration.isInfinite()) {
                await()
                return true
            }
            return memScoped<Boolean> {
                val waitUntil = alloc<timespec>()
                clock_gettime(CLOCK_REALTIME, waitUntil.ptr)
                waitUntil.tv_nsec = (checkTime * 1000000L).convert()

                val now = TimeSource.Monotonic.markNow()
                while (true) {
                    val r = pthread_cond_timedwait(native.cc.ptr, mutex.mm.ptr, waitUntil.ptr)
                    if (Worker.current?.isInterrupted == true) {
                        throw InterruptedException()
                    }
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