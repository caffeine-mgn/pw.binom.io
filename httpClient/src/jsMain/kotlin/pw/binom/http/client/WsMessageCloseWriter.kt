package pw.binom.http.client

import org.w3c.dom.WebSocket
import pw.binom.io.ByteBuffer

class WsMessageCloseWriter(
  private val native: WebSocket,
  private val afterClosed: () -> Unit,
) : AbstractWsMessageWriter() {
  override suspend fun flush() {
    // Do nothing
  }

  override fun send(buffer: ByteBuffer) {
    native.close(reason = buffer.toByteArray().decodeToString())
    afterClosed()
  }
}
