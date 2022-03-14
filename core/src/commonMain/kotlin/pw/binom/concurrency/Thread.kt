package pw.binom.concurrency

import kotlin.time.Duration

expect fun sleep(millis: Long)

fun sleep(duration: Duration) {
    sleep(duration.inWholeMilliseconds)
}
