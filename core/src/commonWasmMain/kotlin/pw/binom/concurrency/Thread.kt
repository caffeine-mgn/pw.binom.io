package pw.binom.concurrency

import kotlin.time.TimeSource

actual fun sleep(millis: Long) {
  val now = TimeSource.Monotonic.markNow()
  while (now.elapsedNow().inWholeMilliseconds < millis) {
    // Do nothing
  }
}
