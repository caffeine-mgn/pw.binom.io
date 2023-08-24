package pw.binom.io.socket

import kotlin.jvm.JvmInline

@JvmInline
value class ListenFlags(val raw: Int) {
  inline val isRead
    get() = raw and KeyListenFlags.READ != 0
  inline val isError
    get() = raw and KeyListenFlags.ERROR != 0
  inline val isWrite
    get() = raw and KeyListenFlags.WRITE != 0
  inline val isOnce
    get() = raw and KeyListenFlags.ONCE != 0

  inline val withRead
    get() = ListenFlags(raw or KeyListenFlags.READ)
  inline val withError
    get() = ListenFlags(raw or KeyListenFlags.ERROR)
  inline val withWrite
    get() = ListenFlags(raw or KeyListenFlags.WRITE)
  inline val withOnce
    get() = ListenFlags(raw or KeyListenFlags.ONCE)

  inline val withoutRead
    get() = ListenFlags((raw.inv() or KeyListenFlags.READ).inv())
  inline val withoutWrite
    get() = ListenFlags((raw.inv() or KeyListenFlags.WRITE).inv())
  inline val withoutError
    get() = ListenFlags((raw.inv() or KeyListenFlags.ERROR).inv())
  inline val withoutOnce
    get() = ListenFlags((raw.inv() or KeyListenFlags.ONCE).inv())

  inline operator fun plus(raw: Int) = this with raw
  inline operator fun minus(raw: Int) = this without raw
  inline infix fun with(raw: Int) = ListenFlags(this.raw or raw)
  inline infix fun without(raw: Int) = ListenFlags((this.raw.inv() or raw).inv())


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
