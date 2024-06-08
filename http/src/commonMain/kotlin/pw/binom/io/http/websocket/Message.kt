package pw.binom.io.http.websocket

import pw.binom.get
import pw.binom.io.AsyncInput
import pw.binom.io.ByteBuffer
import kotlin.experimental.xor

interface Message : AsyncInput {
  val type: MessageType

  companion object {

    fun encode(mask: Int, data: ByteBuffer) {
      val length = data.position
      data.flip()
      encode(0L, mask, data)
      data.position = length
      data.limit = data.capacity
    }

    fun encode(cursor: Long, mask: Int, data: ByteBuffer): Long {
      return MessageCoder.encode(
        cursor = cursor,
        mask = mask,
        data=data
      )
//      var cursorLocal = cursor
//      data.replaceEach { _, byte ->
//        val c = byte xor mask[(cursorLocal and 0x03L).toInt()]
//        cursorLocal++
//        c
//      }
//      return cursorLocal
    }
  }

  val isCloseMessage
    get() = type == MessageType.CLOSE
}
