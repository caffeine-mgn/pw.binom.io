package pw.binom.io.http.websocket

import pw.binom.io.ByteBuffer

expect object MessageCoder {
  /**
   * Кодирует данные с учётом курсора [cursor] и маски [mask] от [data]`.position` до [data]`.limit`.
   * По результату [data]`.position`=[data]`.limit`
   */
  fun encode(cursor: Long, mask: Int, data: ByteBuffer): Long
}
