package pw.binom.io.http.websocket

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pw.binom.InternalLog
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class WebSocketOutput(
  messageType: MessageType,
  masked: Boolean,
  val stream: AsyncOutput,
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
