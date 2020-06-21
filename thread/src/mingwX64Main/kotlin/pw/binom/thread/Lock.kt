package pw.binom.thread

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import platform.posix.free
import platform.posix.malloc
import platform.windows.*
import pw.binom.io.Closeable
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private const val checkTime = 100

actual class Lock : Closeable {

//    val native2 = CreateMutex!!(null, FALSE, null)!!

    private val native = malloc(sizeOf<CRITICAL_SECTION>().convert())!!.reinterpret<CRITICAL_SECTION>()

    init {
        InitializeCriticalSection(native)
    }

    actual fun lock() {
        EnterCriticalSection(native)
    }

    actual fun unlock() {
        LeaveCriticalSection(native)
    }

    override fun close() {
        DeleteCriticalSection(native)
        free(native)
    }

    actual fun newCondition(): Lock.Condition =
            Condition(native)

    actual class Condition(val lock: CPointer<CRITICAL_SECTION>) : Closeable {
        val native = malloc(sizeOf<CONDITION_VARIABLE>().convert())!!.reinterpret<CONDITION_VARIABLE>()

        init {
            InitializeConditionVariable(native)
        }

        actual fun wait() {
            while (true) {
                val r = SleepConditionVariableCS(native, lock, checkTime.convert())
                if (Thread.currentThread.isInterrupted)
                    throw InterruptedException()
                if (r == 0) {
                    val e = GetLastError()
                    if (e != ERROR_TIMEOUT.convert<DWORD>())
                        throw RuntimeException("Error in wait lock. Error: #$e")
                } else break
            }
        }

        actual fun notify() {
            WakeConditionVariable(native)

        }

        actual fun notifyAll() {
            WakeAllConditionVariable(native)
        }

        override fun close() {
            free(native)
        }

        @OptIn(ExperimentalTime::class)
        actual fun wait(duration: Duration): Boolean {
            if (duration.isInfinite()) {
                wait()
                return true
            }
            val now = TimeSource.Monotonic.markNow()
            while (true) {
                val r = SleepConditionVariableCS(native, lock, checkTime.convert())
                if (Thread.currentThread.isInterrupted)
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