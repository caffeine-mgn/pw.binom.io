package pw.binom.io.http.websocket

import pw.binom.atomic.AtomicBoolean
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize

abstract class FrameOutput(
  messageType: MessageType,
  val stream: AsyncOutput,
  bufferSize:Int,
) : AsyncOutput {
  protected var opcode = messageType.opcode
  protected val buffer = ByteBuffer(bufferSize)
  protected val closed = AtomicBoolean(false)
  protected var firstFrame = true

  override suspend fun write(data: ByteBuffer): DataTransferSize {
    if (!buffer.hasRemaining) {
      flush()
    }
    val w = data.read(buffer)
    return w
  }
}
