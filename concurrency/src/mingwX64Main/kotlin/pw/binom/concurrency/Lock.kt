package pw.binom.concurrency

import kotlinx.cinterop.*
import platform.windows.*
import pw.binom.atomic.AtomicInt
import pw.binom.io.Closeable
import kotlin.native.concurrent.freeze
import kotlin.native.internal.createCleaner
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private const val checkTime = 100

@OptIn(ExperimentalStdlibApi::class)
actual class Lock : Closeable {

    //    val native2 = CreateMutex!!(null, FALSE, null)!!
    var closed = AtomicInt(0)

    private val native = nativeHeap.alloc<CRITICAL_SECTION>()

    init {
        InitializeCriticalSection(native.ptr)
        createCleaner(native) { native ->
            DeleteCriticalSection(native.ptr)
            nativeHeap.free(native)
        }
    }

    actual fun lock() {
        EnterCriticalSection(native.ptr)
    }

    actual fun unlock() {
        LeaveCriticalSection(native.ptr)
    }

    override fun close() {
        if (closed.value == 1)
            throw IllegalStateException("Lock already closed")

        closed.value = 1
    }

    actual fun newCondition(): Condition =
        Condition(native)

    actual class Condition(val lock: CRITICAL_SECTION) : Closeable {
        val native =
            nativeHeap.alloc<CONDITION_VARIABLE>()

        init {
            InitializeConditionVariable(native.ptr)
            createCleaner(native) { native ->
                nativeHeap.free(native)
            }
        }

        actual fun await() {
            while (true) {
                val r = SleepConditionVariableCS(native.ptr, lock.ptr, checkTime.convert())
                if (Worker.current?.isInterrupted == true) {
                    throw InterruptedException()
                }
                if (r == 0) {
                    val e = GetLastError()
                    if (e != ERROR_TIMEOUT.convert<DWORD>()) {
                        throw RuntimeException("Error in wait lock. Error: #$e")
                    }
                } else break
            }
        }

        actual fun signal() {
            WakeConditionVariable(native.ptr)
        }

        actual fun signalAll() {
            WakeAllConditionVariable(native.ptr)
        }

        override fun close() {

        }

        @OptIn(ExperimentalTime::class)
        actual fun await(duration: Duration): Boolean {
            if (duration.isInfinite()) {
                await()
                return true
            }
            val now = TimeSource.Monotonic.markNow()
            while (true) {
                val r = SleepConditionVariableCS(native.ptr, lock.ptr, checkTime.convert())
                if (Worker.current?.isInterrupted == true) {
                    throw InterruptedException()
                }
                if (r == 0) {
                    val e = GetLastError()
                    if (e != ERROR_TIMEOUT.convert<DWORD>()) {
                        throw RuntimeException("Error in wait lock. Error: #$e")
                    }

                    if (now.elapsedNow() > duration) {
                        return false
                    }
                } else {
                    break
                }
            }
            return true
        }
    }
}