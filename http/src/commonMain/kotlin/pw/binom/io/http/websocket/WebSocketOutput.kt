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
  private var closed = false

  private fun checkClosed() {
    if (closed) {
      throw StreamClosedException()
    }
  }

  private val opcode
    get() = if (first) {
      messageType.opcode
    } else {
      Opcode.CONTINUATION
    }

  override suspend fun flush() {
    if (flush(eof = false)) {
      first = false
    }
  }

  private suspend fun flush(eof: Boolean): Boolean {
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
        opcode = opcode,
        length = length.toLong(),
        maskFlag = masked,
        mask = mask,
        finishFlag = eof,
      )
      super.flush()
      return true
    }
    return false
  }

  override suspend fun asyncClose() {
    checkClosed()
    try {
      val finishSent = flush(eof = true)
      val needSendEnd = !finishSent
      if (needSendEnd) {
        val mask = if (masked) {
          Random.nextInt()
        } else {
          0
        }
        WebSocketHeader.write(
          output = stream,
          opcode = opcode,
          length = 0,
          maskFlag = masked,
          mask = mask,
          finishFlag = true,
        )
        stream.flush()
      }
    } finally {
      super.asyncClose()
      writeLock?.unlock()
      buffer.close()
      closed = true
    }
  }
}
