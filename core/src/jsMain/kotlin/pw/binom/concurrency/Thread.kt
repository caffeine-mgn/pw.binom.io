package pw.binom.concurrency

import kotlin.js.Date

actual fun sleep(millis: Long) {
    val endTime = Date.now() + millis * 0.001f
    while (Date.now() < endTime) {
        // Do nothing
    }
}
