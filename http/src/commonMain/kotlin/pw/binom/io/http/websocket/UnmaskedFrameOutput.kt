package pw.binom.io.http.websocket

import pw.binom.io.AsyncOutput

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
      WsDetectSlow("UnmaskedFrameOutput::asyncClose #1") {
        WebSocketHeader.write(
          output = stream,
          opcode = opcode,
          length = buffer.remaining.toLong(),
          maskFlag = false,
          mask = 0,
          finishFlag = true,
        )
      }
      WsDetectSlow("UnmaskedFrameOutput::asyncClose #2, stream=${stream::class}") {
        stream.writeFully(buffer)
      }
    } else {
      WsDetectSlow("UnmaskedFrameOutput::asyncClose #3") {
        WebSocketHeader.write(
          output = stream,
          opcode = opcode,
          length = 0,
          maskFlag = false,
          mask = 0,
          finishFlag = true,
        )
      }
    }
    WsDetectSlow("UnmaskedFrameOutput::asyncClose #4") {
      stream.flush()
    }
    buffer.close()
  }

  override suspend fun flush() {
    if (buffer.position == 0) {
      return
    }
    buffer.flip()
    WsDetectSlow("UnmaskedFrameOutput::flush #1") {
      WebSocketHeader.write(
        output = stream,
        opcode = opcode,
        length = buffer.remaining.toLong(),
        maskFlag = true,
        mask = 0,
        finishFlag = false,
      )
    }
    WsDetectSlow("UnmaskedFrameOutput::flush #2 stream=${stream::class}") {
      stream.writeFully(buffer)
    }
    opcode = Opcode.CONTINUATION
    firstFrame = false
    WsDetectSlow("UnmaskedFrameOutput::flush #3 stream=${stream::class}") {
      stream.flush()
    }
    buffer.clear()
  }
}
