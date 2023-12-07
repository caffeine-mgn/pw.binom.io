package pw.binom.io.socket

import kotlin.jvm.JvmInline

@JvmInline
value class ListenFlags(val raw: Int) {
  companion object {
    val ZERO = ListenFlags()
    val WRITE = ListenFlags().withWrite
    val READ = ListenFlags().withRead
    val ONCE = ListenFlags().withOnce
    val ERROR = ListenFlags().withError
  }

  constructor() : this(0)

  inline val isRead
    get() = raw and KeyListenFlags.READ != 0
  inline val isError
    get() = raw and KeyListenFlags.ERROR != 0
  inline val isWrite
    get() = raw and KeyListenFlags.WRITE != 0
  inline val isOnce
    get() = raw and KeyListenFlags.ONCE != 0

  inline val withRead
    get() = this with KeyListenFlags.READ
  inline val withError
    get() = this with KeyListenFlags.ERROR
  inline val withWrite
    get() = this with KeyListenFlags.WRITE
  inline val withOnce
    get() = this with KeyListenFlags.ONCE

  inline val withoutRead
    get() = this without KeyListenFlags.READ
  inline val withoutWrite
    get() = this without KeyListenFlags.WRITE
  inline val withoutError
    get() = this without KeyListenFlags.ERROR
  inline val withoutOnce
    get() = this without KeyListenFlags.ONCE

  inline operator fun plus(flags: ListenFlags) = this with flags.raw
  inline operator fun minus(flags: ListenFlags) = this without flags.raw
  inline operator fun plus(raw: Int) = this with raw
  inline operator fun minus(raw: Int) = this without raw
  inline infix fun with(raw: Int) = ListenFlags(this.raw or raw)
  inline infix fun without(raw: Int) = ListenFlags((this.raw.inv() or raw).inv())

  operator fun contains(other: ListenFlags) = this.raw and other.raw != 0

  override fun toString(): String {
    val sb = StringBuilder()
    var started = false
    fun start() {
      if (!started) {
        started = true
      } else {
        sb.append(" ")
      }
    }
    if (isRead) {
      start()
      sb.append("READ")
    }
    if (isWrite) {
      start()
      sb.append("WRITE")
    }
    if (isError) {
      start()
      sb.append("ERROR")
    }
    if (isOnce) {
      start()
      sb.append("ONCE")
    }
    start()
    sb.append("0x${raw.toUInt().toString(16)} ${raw.toUInt().toString(2)}")
    return sb.toString()
  }
}
