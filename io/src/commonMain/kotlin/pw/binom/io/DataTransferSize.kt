package pw.binom.io

import kotlin.jvm.JvmInline

@JvmInline
value class DataTransferSize private constructor(private val raw: Int) {
  companion object {
    val EMPTY = DataTransferSize(0)
    val CLOSED = DataTransferSize(-1)
    fun ofSize(length: Int) = when {
      length == 0 -> EMPTY
      length < 0 -> CLOSED
      else -> DataTransferSize(length)
    }
  }

  val isEof get() = raw == 0
  val isClosed get() = raw < 0
  val isAvailable get() = raw > 0
  val isNotAvailable get() = raw <= 0
  val length
    get() = when {
      isClosed -> throw IllegalStateException("No data. Stream is closed")
      else -> raw
    }

  override fun toString() = when {
    isEof -> "EOF"
    isClosed -> "CLOSED"
    else -> raw.toString()
  }
}
