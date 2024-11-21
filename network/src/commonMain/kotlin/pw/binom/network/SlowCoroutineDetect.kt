package pw.binom.network

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pw.binom.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

inline fun <T> detectSlow(msg: String, duration: Duration = 1.seconds, func: () -> T): T {
  val stackTrace = Throwable()
  val finished = AtomicBoolean(false)
  GlobalScope.launch {
    delay(duration)
    if (!finished.getValue()) {
      println("Slow: $msg\n${stackTrace.stackTraceToString()}")
    }
  }

  return try {
    func()
  } finally {
    finished.setValue(true)
  }
}
