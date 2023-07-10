package pw.binom.concurrency

import kotlinx.cinterop.*
import platform.windows.*
import kotlin.native.internal.Cleaner
import kotlin.native.internal.createCleaner
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private const val checkTime = 50

@OptIn(ExperimentalStdlibApi::class)
actual class ReentrantLock : Lock {

  private val native = nativeHeap.alloc<CRITICAL_SECTION>()

  init {
    InitializeCriticalSection(native.ptr)
  }

  private val cleaner = createCleaner(native) { native ->
    LeaveCriticalSection(native.ptr)
    DeleteCriticalSection(native.ptr)
    nativeHeap.free(native)
  }

  override fun tryLock(): Boolean = TryEnterCriticalSection(native.ptr) != 0

  override fun lock() {
    EnterCriticalSection(native.ptr)
  }

  override fun unlock() {
    LeaveCriticalSection(native.ptr)
  }

  actual fun newCondition(): Condition =
    Condition(cleaner, native)

  actual class Condition(val lockCleaner: Cleaner, val lock: CRITICAL_SECTION) {
    val native =
      nativeHeap.alloc<CONDITION_VARIABLE>()

    init {
      InitializeConditionVariable(native.ptr)
    }

    internal val cleaner = createCleaner(native) { native ->
      nativeHeap.free(native)
    }

    actual fun await() {
      while (true) {
        val r = SleepConditionVariableCS(native.ptr, lock.ptr, checkTime.convert())
        if (Worker.current?.isInterrupted == true) {
          throw InterruptedException()
        }
        if (r == 0) {
          val e = GetLastError()
          if (e != ERROR_TIMEOUT.convert<DWORD>() && e != 0.convert<DWORD>()) {
            throw RuntimeException("Error in wait lock. Error: #$e")
          }
        } else {
          break
        }
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
          if (e != ERROR_TIMEOUT.convert<DWORD>() && e != 0.convert<DWORD>()) {
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
