package pw.binom.io.http.websocket

enum class MessageType(val opcode: Opcode) {
  BINARY(opcode = Opcode.BINARY),
  TEXT(opcode = Opcode.TEXT),
  CLOSE(opcode = Opcode.CLOSE),
}
