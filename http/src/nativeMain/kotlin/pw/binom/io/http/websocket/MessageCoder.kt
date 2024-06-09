package pw.binom.io.http.websocket

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import platform.websocket.internalMessageCoderEncode
import pw.binom.io.ByteBuffer

actual object MessageCoder {
  @OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
  actual fun encode(cursor: Long, mask: Int, data: ByteBuffer): Long {
    return internalMessageCoderEncode(
      data = data.data.pointer,
      start = data.position,
      limit = data.limit,
      cursor = cursor,
      mask = mask,
    )
  }
}
