package pw.binom.io.http.websocket

import pw.binom.get
import pw.binom.io.ByteBuffer
import kotlin.experimental.xor

actual object MessageCoder {
  actual fun encode(cursor: Long, mask: Int, data: ByteBuffer): Long{
    var cursorLocal = cursor
    data.replaceEach { _, byte ->
      val c = byte xor mask[(cursorLocal and 0x03L).toInt()]
      cursorLocal++
      c
    }
    return cursorLocal
  }
}
