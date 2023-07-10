package pw.binom.io.http.websocket

import pw.binom.coroutines.SimpleAsyncLock
import pw.binom.io.*
import kotlin.random.Random

class WebSocketOutput(
  val messageType: MessageType,
  val masked: Boolean,
  override val stream: AsyncOutput,
  bufferSize: Int,
  val writeLock: SimpleAsyncLock?,
) : AbstractAsyncBufferedOutput() {
  override val buffer: ByteBuffer = ByteBuffer(bufferSize).empty()

  private var first = true
  private var eof = false
  private var closed = false

  private fun checkClosed() {
    if (closed) {
      throw StreamClosedException()
    }
  }

  override suspend fun flush() {
    checkClosed()
    if (buffer.remaining < buffer.capacity) {
//      val v = WebSocketHeader()
//      v.finishFlag = eof
      val length = buffer.position
//      v.maskFlag = masked
      val mask = if (masked) {
        val mask = Random.nextInt()
        Message.encode(mask, buffer)
        mask
      } else {
        0
      }
//      v.length = length.toLong()
//      v.opcode = if (!first) {
//        Opcode.ZERO
//      } else {
//        messageType.opcode
//      }
//      WebSocketHeader.write(stream, v)
      WebSocketHeader.write(
        output = stream,
        opcode = if (!first) {
          Opcode.ZERO
        } else {
          messageType.opcode
        },
        length = length.toLong(),
        maskFlag = masked,
        mask = mask,
        finishFlag = eof,
      )
      first = false
    }
    super.flush()
  }

  override suspend fun asyncClose() {
    checkClosed()
    try {
      flush()
      val needSendEnd = buffer.remaining < buffer.capacity && !first
      eof = true
      super.asyncClose()

      if (needSendEnd) {
//        val v = WebSocketHeader()
//        v.opcode = Opcode.ZERO
//        v.length = 0L
//        v.maskFlag = masked
        val mask = if (masked) {
          Random.nextInt()
        } else {
          0
        }
//        v.finishFlag = true
//        WebSocketHeader.write(stream, v)
        WebSocketHeader.write(
          output = stream,
          opcode = Opcode.ZERO,
          length = 0,
          maskFlag = masked,
          mask = mask,
          finishFlag = true,
        )
        stream.flush()
      }
    } finally {
      writeLock?.unlock()
      buffer.close()
      closed = true
    }
  }
}
