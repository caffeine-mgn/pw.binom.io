package pw.binom.concurrency

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

expect fun sleep(millis: Long)

@OptIn(ExperimentalTime::class)
fun sleep(duration: Duration) {
    sleep(duration.inWholeMilliseconds)
}