package pw.binom.io

import kotlin.jvm.JvmInline

@JvmInline
value class Available(val raw: Int) {
  companion object {
    fun of(value: Int) = Available(value)
    fun isAvailable(value: Int): Available {
      require(value > 0)
      return Available(value)
    }

    val UNKNOWN = Available(-1)
    val NOT_AVAILABLE = Available(0)
  }

  val isUnknown
    get() = raw <= -1
  val isAvailable
    get() = raw > 0
  val isNotAvailable
    get() = raw == 0

  val toInt
    get() = raw
}


fun minOf(a: Available, b: Available) = Available.of(minOf(a.raw, b.raw))
