package pw.binom.io.http.websocket

import pw.binom.atomic.AtomicBoolean
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.holdState
import kotlin.random.Random

class MaskedFrameOutput(
  messageType: MessageType,
  bufferSize: Int,
  stream: AsyncOutput,
) : FrameOutput(
  stream = stream,
  messageType = messageType,
  bufferSize = bufferSize,
) {
  private var mask: Int = Random.nextInt()
  private var cursor = 0L

  override suspend fun asyncClose() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    if (buffer.position > 0) {
      buffer.flip()
      buffer.holdState {
        cursor = MessageCoder.encode(
          cursor = cursor,
          mask = mask,
          data = buffer,
        )
      }
      WebSocketHeader.write(
        output = stream,
        opcode = opcode,
        length = buffer.remaining.toLong(),
        maskFlag = true,
        mask = mask,
        finishFlag = true,
      )
      stream.writeFully(buffer)
    } else {
      WebSocketHeader.write(
        output = stream,
        opcode = opcode,
        length = 0,
        maskFlag = true,
        mask = mask,
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
    buffer.holdState {
      cursor = MessageCoder.encode(
        cursor = cursor,
        mask = mask,
        data = buffer,
      )
    }
    WebSocketHeader.write(
      output = stream,
      opcode = opcode,
      length = buffer.remaining.toLong(),
      maskFlag = true,
      mask = mask,
      finishFlag = false,
    )
    stream.writeFully(buffer)
    mask = Random.nextInt()
    cursor = 0
    opcode = Opcode.CONTINUATION
    firstFrame = false
    buffer.clear()
    stream.flush()
  }
}
