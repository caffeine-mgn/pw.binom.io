package pw.binom.io.http.websocket

enum class MessageType(val opcode: Opcode) {
  CONTINUATION(opcode = Opcode.CONTINUATION),
  BINARY(opcode = Opcode.BINARY),
  TEXT(opcode = Opcode.TEXT),
  CLOSE(opcode = Opcode.CLOSE),
  PING(opcode = Opcode.PING),
  PONG(opcode = Opcode.PONG),
}
