package pw.binom.date

import kotlin.jvm.JvmInline
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

@JvmInline
value class Time(val nano: Long) {
  companion object {
    fun fromMilliseconds(milliseconds: Long) = Time(milliseconds.milliseconds.inWholeNanoseconds)
  }

  val duration
    get() = nano.nanoseconds

  val milliseconds
    get() = duration.inWholeMilliseconds
}
