package pw.binom.concurrency

import kotlinx.cinterop.*
import platform.windows.*
import pw.binom.atomic.AtomicInt
import pw.binom.io.Closeable
import pw.binom.thread.InterruptedException
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private const val checkTime = 100

actual class Lock : Closeable {

    //    val native2 = CreateMutex!!(null, FALSE, null)!!
    var closed = AtomicInt(0)

    private val native = nativeHeap.alloc<CRITICAL_SECTION>()

    init {
        InitializeCriticalSection(native.ptr)
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
        DeleteCriticalSection(native.ptr)
        nativeHeap.free(native)
        closed.value = 1
    }

    actual fun newCondition(): Condition =
            Condition(native)

    actual class Condition(val lock: CRITICAL_SECTION) : Closeable {
        val native = nativeHeap.alloc<CONDITION_VARIABLE>()//malloc(sizeOf<CONDITION_VARIABLE>().convert())!!.reinterpret<CONDITION_VARIABLE>()

        init {
            InitializeConditionVariable(native.ptr)
        }

        actual fun wait() {
            while (true) {
                val r = SleepConditionVariableCS(native.ptr, lock.ptr, checkTime.convert())
                if (Worker.current?.isInterrupted == true)
                    throw InterruptedException()
                if (r == 0) {
                    val e = GetLastError()
                    if (e != ERROR_TIMEOUT.convert<DWORD>())
                        throw RuntimeException("Error in wait lock. Error: #$e")
                } else break
            }
        }

        actual fun notify() {
            WakeConditionVariable(native.ptr)
        }

        actual fun notifyAll() {
            WakeAllConditionVariable(native.ptr)
        }

        override fun close() {
            nativeHeap.free(native)
        }

        @OptIn(ExperimentalTime::class)
        actual fun wait(duration: Duration): Boolean {
            if (duration.isInfinite()) {
                wait()
                return true
            }
            val now = TimeSource.Monotonic.markNow()
            while (true) {
                val r = SleepConditionVariableCS(native.ptr, lock.ptr, checkTime.convert())
                if (Worker.current?.isInterrupted == true)
                    throw InterruptedException()
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