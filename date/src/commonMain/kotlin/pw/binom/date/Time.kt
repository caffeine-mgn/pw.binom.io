package pw.binom.date

import kotlin.jvm.JvmInline
import kotlin.time.Duration.Companion.nanoseconds

@JvmInline
value class Time(val nano: Long) {
    val duration
        get() = nano.nanoseconds

    val milliseconds
        get() = duration.inWholeMilliseconds
}
