package pw.binom.io.http.websocket

import kotlin.jvm.JvmInline

@JvmInline
value class Opcode(val raw: Byte) {
  companion object {
    val ZERO = Opcode(raw = 0)
    val TEXT = Opcode(raw = 1)
    val BINARY = Opcode(raw = 2)
    val CLOSE = Opcode(raw = 8)
  }

  fun toMessageType() =
    when (raw) {
      TEXT.raw -> MessageType.TEXT
      BINARY.raw -> MessageType.BINARY
      CLOSE.raw -> MessageType.CLOSE
      else -> throw IllegalArgumentException("Unknown opcode $raw")
    }
}
