package pw.binom.atomic

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
inline fun <T> AtomicBoolean.synchronize(func: () -> T): T {
//    val bb = TimeSource.Monotonic.markNow()
//    while (true) {
//        if (compareAndSet(false, true)) {
//            break
//        }
//        if (bb.elapsedNow() > 10.seconds) {
//            println("SpinLock->Lock timeout!!!\n${Throwable().stackTraceToString()}")
//        }
//    }
  val now = TimeSource.Monotonic.markNow()
  while (true) {
    if (tryLock()) {
      break
    }
    if (now.elapsedNow() > 5.seconds) {
      throw RuntimeException("Timeout")
    }
  }
  try {
    return func()
  } finally {
    unlock()
//        setValue(false)
  }
}

@Suppress("NOTHING_TO_INLINE")
inline fun AtomicBoolean.lock(timeout: Duration) {
  val bb = TimeSource.Monotonic.markNow()
  while (true) {
    if (compareAndSet(false, true)) {
      break
    }
    if (bb.elapsedNow() > timeout) {
      throw RuntimeException("Timeout $timeout")
    }
  }
}

fun AtomicBoolean.tryLock() = compareAndSet(false, true)

@Suppress("NOTHING_TO_INLINE")
inline fun AtomicBoolean.lock() {
  while (true) {
    if (tryLock()) {
      break
    }
  }
}

@Suppress("NOTHING_TO_INLINE")
inline fun AtomicBoolean.unlock() {
  setValue(false)
}
