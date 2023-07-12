package pw.binom.io.http.websocket

import kotlin.jvm.JvmInline

@JvmInline
value class Opcode(val raw: Byte) {
  companion object {
    val CONTINUATION = Opcode(raw = 0)
    val TEXT = Opcode(raw = 1)
    val BINARY = Opcode(raw = 2)
    val CLOSE = Opcode(raw = 8)
    val PING = Opcode(raw = 9)
    val PONG = Opcode(raw = 10) // A
  }

  fun toMessageType() =
    when (raw) {
      CONTINUATION.raw -> MessageType.CONTINUATION
      TEXT.raw -> MessageType.TEXT
      BINARY.raw -> MessageType.BINARY
      CLOSE.raw -> MessageType.CLOSE
      PING.raw -> MessageType.PING
      PONG.raw -> MessageType.PONG
      else -> throw IllegalArgumentException("Unknown opcode $raw")
    }
}
