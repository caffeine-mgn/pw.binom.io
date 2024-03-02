package pw.binom.mq

import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteBuffer

interface Producer : AsyncCloseable {
  companion object;

  suspend fun send(
    headers: Headers = Headers.empty,
    data: ByteArray,
  )

  suspend fun send(
    headers: Headers = Headers.empty,
    data: ByteBuffer,
  )
}
