package pw.binom.io.http.websocket

import pw.binom.get
import pw.binom.io.ByteBuffer
import kotlin.experimental.xor

actual object MessageCoder {
  actual fun encode(cursor: Long, mask: Int, data: ByteBuffer): Long {
    var cursorLocal = cursor
    var counter = cursorLocal.toInt()
    data.replaceEach { _, byte ->
      val c = byte xor mask[counter and 0x03]
      counter++
      cursorLocal++
      c
    }
    data.position = data.limit
    return cursorLocal
  }
}
