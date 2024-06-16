package pw.binom.io.http.websocket

import pw.binom.io.AsyncInput
import pw.binom.io.ByteBuffer

interface WebSocketInput : AsyncInput {
  val type: MessageType

  companion object {

    fun encode(mask: Int, data: ByteBuffer) {
      val length = data.position
      data.flip()
      encode(0L, mask, data)
      data.position = length
      data.limit = data.capacity
    }

    fun encode(cursor: Long, mask: Int, data: ByteBuffer): Long = MessageCoder.encode(
      cursor = cursor,
      mask = mask,
      data = data
    )
  }

  val isCloseMessage
    get() = type == MessageType.CLOSE
}
