package pw.binom.concurrency

import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
actual fun sleep(millis: Long) {
    val now = TimeSource.Monotonic.markNow()
    while (now.elapsedNow().inWholeMinutes < millis) {
        // do nothing
    }
}
