package pw.binom.io.http.websocket

import kotlinx.cinterop.ExperimentalForeignApi
import platform.websocket.internalMessageCoderEncode
import pw.binom.io.ByteBuffer

actual object MessageCoder {
  @OptIn(ExperimentalForeignApi::class)
  actual fun encode(cursor: Long, mask: Int, data: ByteBuffer): Long {
    val newCounter = internalMessageCoderEncode(
      data = data.data.pointer,
      start = data.position,
      limit = data.limit,
      cursor = cursor,
      mask = mask,
    )
    data.position = data.limit
    return newCounter
  }
}
