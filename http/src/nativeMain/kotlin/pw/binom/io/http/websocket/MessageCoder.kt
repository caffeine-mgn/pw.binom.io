package pw.binom.io.http.websocket

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.set
import pw.binom.io.ByteBuffer
import kotlin.experimental.xor

actual object MessageCoder {
  actual fun encode(mask: Int, data: ByteBuffer) {
    val length = data.position
    data.flip()
    Message.encode(0L, mask, data)
    data.position = length
    data.limit = data.capacity
  }

  @OptIn(ExperimentalForeignApi::class)
  actual fun encode(cursor: Long, mask: Int, data: ByteBuffer): Long {
    var globalCursor = cursor
    val end = data.limit
    var localCursor = data.position
    while (localCursor < end) {
      val maskIndex = (globalCursor.toInt() and 0x03)
      val maskValue = ((mask ushr (8 * (3 - maskIndex)))).toByte()
      val result = data.data.pointer[localCursor] xor maskValue
      data.data.pointer[localCursor] = result
      localCursor++
      globalCursor++
    }
    return globalCursor
  }
}
