package pw.binom.io.http.websocket

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.holdState
import kotlin.random.Random

class UnmaskedFrameOutput(
  messageType: MessageType,
  stream: AsyncOutput,
  bufferSize: Int,
) : FrameOutput(
  stream = stream,
  messageType = messageType,
  bufferSize = bufferSize,
) {
  override suspend fun asyncClose() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    if (buffer.position > 0) {
      buffer.flip()
      WebSocketHeader.write(
        output = stream,
        opcode = opcode,
        length = buffer.remaining.toLong(),
        maskFlag = false,
        mask = 0,
        finishFlag = true,
      )
      stream.writeFully(buffer)
    } else {
      WebSocketHeader.write(
        output = stream,
        opcode = opcode,
        length = 0,
        maskFlag = false,
        mask = 0,
        finishFlag = true,
      )
    }
    stream.flush()
    buffer.close()
  }

  override suspend fun flush() {
    if (buffer.position == 0) {
      return
    }
    buffer.flip()
    WebSocketHeader.write(
      output = stream,
      opcode = opcode,
      length = buffer.remaining.toLong(),
      maskFlag = true,
      mask = 0,
      finishFlag = false,
    )
    stream.writeFully(buffer)
    opcode = Opcode.CONTINUATION
    firstFrame = false
    stream.flush()
    buffer.clear()
  }
}
