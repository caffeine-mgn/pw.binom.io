package pw.binom.io.http.websocket

import pw.binom.io.ByteBuffer

expect object MessageCoder {
  fun encode(cursor: Long, mask: Int, data: ByteBuffer): Long
}
