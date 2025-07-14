package pw.binom.http.client

import org.w3c.dom.WebSocket
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteArrayOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize

internal class WsMessageWriter(
  val binary: Boolean,
  private val native: WebSocket,
) : AbstractWsMessageWriter() {

  override fun send(buffer: ByteBuffer) {
    if (binary) {
      val arr = buffer.native.toInt8Array(startIndex = buffer.position, endIndex = buffer.limit)
      native.send(arr)
    } else {
      native.send(buffer.toByteArray().decodeToString())
    }
  }
}
