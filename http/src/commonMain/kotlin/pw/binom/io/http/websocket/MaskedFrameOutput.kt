package pw.binom.io.http.websocket

import pw.binom.io.AsyncOutput
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
      WsDetectSlow("MaskedFrameOutput::asyncClose #1") {
        buffer.holdState {
          cursor = MessageCoder.encode(
            cursor = cursor,
            mask = mask,
            data = buffer,
          )
        }
      }
      WsDetectSlow("MaskedFrameOutput::asyncClose #2") {
        WebSocketHeader.write(
          output = stream,
          opcode = opcode,
          length = buffer.remaining.toLong(),
          maskFlag = true,
          mask = mask,
          finishFlag = true,
        )
      }
      WsDetectSlow("MaskedFrameOutput::asyncClose #3") {
        stream.writeFully(buffer)
      }
    } else {
      WsDetectSlow("MaskedFrameOutput::asyncClose #4") {
        WebSocketHeader.write(
          output = stream,
          opcode = opcode,
          length = 0,
          maskFlag = true,
          mask = mask,
          finishFlag = true,
        )
      }
    }
    WsDetectSlow("MaskedFrameOutput::asyncClose #5 stream=${stream::class}") {
      stream.flush()
    }
    buffer.close()
  }

  override suspend fun flush() {
    if (buffer.position == 0) {
      return
    }
    buffer.flip()
    WsDetectSlow("MaskedFrameOutput::flush #1") {
      buffer.holdState {
        cursor = MessageCoder.encode(
          cursor = cursor,
          mask = mask,
          data = buffer,
        )
      }
    }
    WsDetectSlow("MaskedFrameOutput::flush #2") {
      WebSocketHeader.write(
        output = stream,
        opcode = opcode,
        length = buffer.remaining.toLong(),
        maskFlag = true,
        mask = mask,
        finishFlag = false,
      )
    }
    WsDetectSlow("MaskedFrameOutput::flush #3") {
      stream.writeFully(buffer)
    }
    mask = Random.nextInt()
    cursor = 0
    opcode = Opcode.CONTINUATION
    firstFrame = false
    buffer.clear()
    WsDetectSlow("MaskedFrameOutput::flush #3") {
      stream.flush()
    }
  }
}
