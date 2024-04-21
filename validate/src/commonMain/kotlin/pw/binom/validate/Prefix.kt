package pw.binom.validate

import kotlin.jvm.JvmInline

@JvmInline
internal value class Prefix private constructor(private val raw: String) {
  companion object {
    val EMPTY = Prefix("")
  }

  fun with(field: String) = if (raw.isEmpty()) {
    Prefix(field)
  } else {
    Prefix("$raw.$field")
  }

  override fun toString(): String = raw
  operator fun plus(field: String) = with(field)
}
