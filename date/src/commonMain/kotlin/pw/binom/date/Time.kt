package pw.binom.date

import kotlin.jvm.JvmInline
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

@JvmInline
value class Time(val nano: Long) {
  companion object {
    fun fromMilliseconds(milliseconds: Long) = Time(milliseconds.milliseconds.inWholeNanoseconds)
    val now
      get() = fromMilliseconds(DateTime.nowTime % MILLISECONDS_IN_DAY)

    fun of(
      hours: Int = 0,
      minutes: Int = 0,
      seconds: Int = 0,
      millis: Int = 0,
    ) = fromMilliseconds(
      hours * 60L * 60L * 1000L +
        minutes * 60L * 1000L +
        seconds * 1000L +
        millis
    )
  }

  val duration
    get() = nano.nanoseconds

  val milliseconds
    get() = duration.inWholeMilliseconds
}
