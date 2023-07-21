package pw.binom.atomic

import kotlin.time.Duration
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
  lock()
  try {
    return func()
  } finally {
    unlock()
//        setValue(false)
  }
}

@OptIn(ExperimentalTime::class)
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

inline fun AtomicBoolean.lock() {
  while (true) {
    if (compareAndSet(false, true)) {
      break
    }
//        println("AtomicBoolean::lock wait unlocking...")
  }
}

inline fun AtomicBoolean.unlock() {
  setValue(false)
}
