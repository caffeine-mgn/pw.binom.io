package pw.binom.io.http.websocket

import pw.binom.atomic.AtomicBoolean
import pw.binom.io.*
import kotlin.random.Random

internal class WebSocketOutput(
  val messageType: MessageType,
  val masked: Boolean,
  override val stream: AsyncOutput,
  bufferSize: Int,
  val connection:WebSocketConnectionImpl,
) : AbstractAsyncBufferedOutput() {

  override val buffer: ByteBuffer = ByteBuffer(bufferSize).empty()

  private var first = true
  private var closed = false
  private var closing = AtomicBoolean(false)

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
    flush(eof = false)
  }

  override suspend fun write(data: ByteBuffer): Int {
    if (closing.getValue()) {
      return -1
    }
    return super.write(data)
  }

  private suspend fun internalFlush(eof: Boolean): Boolean {
    if (!hasDataForFlush) {
      return false
    }
    val length = buffer.position
    val mask = if (masked) {
      val mask = Random.nextInt()
      WebSocketInput.encode(mask, buffer)
      mask
    } else {
      0
    }
    WebSocketHeader.write(
      output = stream,
      opcode = opcode,
      length = length.toLong(),
      maskFlag = masked,
      mask = mask,
      finishFlag = eof,
    )
    first = false
    flushWithoutCloseCheck()
    return true
  }

  private suspend fun flush(eof: Boolean): Boolean {
    checkClosed()
    return internalFlush(eof)
  }

  private fun clearBuffer() {
    buffer.close()
  }

  override suspend fun asyncClose() {
    if (!closing.compareAndSet(false, true)) {
      return
    }
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
      clearBuffer()
      closed = true
      connection.writingMessageFinished()
    }
  }
}
