package pw.binom

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

actual val Environment.workDirectory: String
    get() = ""

private val nowValue = TimeSource.Monotonic.markNow()

actual val Environment.currentTimeMillis: Long
    get() = nowValue.elapsedNow().inWholeMilliseconds

actual val Environment.currentTimeNanoseconds: Long
  get() = currentTimeMillis.milliseconds.inWholeNanoseconds

actual val Environment.currentExecutionPath: String
    get() = ""
