package pw.binom.io.http.websocket

import pw.binom.atomic.AtomicBoolean
import pw.binom.io.*

internal class WebSocketOutput(
  messageType: MessageType,
  masked: Boolean,
  stream: AsyncOutput,
  bufferSize: Int,
  val connection: WebSocketConnectionImpl,
) : AsyncOutput {

  private val output = if (masked) {
    MaskedFrameOutput(
      messageType = messageType,
      stream = stream,
      bufferSize = bufferSize,
    )
  } else {
    UnmaskedFrameOutput(
      messageType = messageType,
      stream = stream,
      bufferSize = bufferSize,
    )
  }


  private var closing = AtomicBoolean(false)


  override suspend fun flush() {
    output.flush()
  }

  override suspend fun write(data: ByteBuffer): DataTransferSize =
    output.write(data)

  override suspend fun asyncClose() {
    if (!closing.compareAndSet(false, true)) {
      return
    }
    try {
      output.asyncClose()
    } finally {
      connection.writingMessageFinished()
    }
  }
}
