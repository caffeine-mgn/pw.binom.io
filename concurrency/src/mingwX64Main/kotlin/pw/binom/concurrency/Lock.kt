package pw.binom.concurrency

import kotlinx.cinterop.*
import platform.windows.*
import kotlin.native.concurrent.freeze
import kotlin.native.internal.createCleaner
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private const val checkTime = 50

@OptIn(ExperimentalStdlibApi::class)
actual class Lock {

    private val native = nativeHeap.alloc<CRITICAL_SECTION>()

    init {
        InitializeCriticalSection(native.ptr)

    }

    private val cleaner = createCleaner(native) { native ->
        DeleteCriticalSection(native.ptr)
        nativeHeap.free(native)
    }

    init {
        freeze()
    }

    actual fun lock() {
        EnterCriticalSection(native.ptr)
    }

    actual fun unlock() {
        LeaveCriticalSection(native.ptr)
    }

    actual fun newCondition(): Condition =
        Condition(native)

    actual class Condition(val lock: CRITICAL_SECTION) {
        val native =
            nativeHeap.alloc<CONDITION_VARIABLE>()

        init {
            InitializeConditionVariable(native.ptr)
        }

        private val cleaner = createCleaner(native) { native ->
            nativeHeap.free(native)
        }

        init {
            freeze()
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

        @OptIn(ExperimentalTime::class)
        actual fun await(duration: Duration): Boolean {
            if (duration.isInfinite()) {
                await()
                return true
            }
            val now = TimeSource.Monotonic.markNow()
            while (true) {
                val r = SleepConditionVariableCS(native.ptr, lock.ptr, duration.inWholeMilliseconds.convert())
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