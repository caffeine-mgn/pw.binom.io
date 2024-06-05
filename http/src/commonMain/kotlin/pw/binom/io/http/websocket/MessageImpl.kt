package pw.binom.io.http.websocket

/*
import pw.binom.copyTo
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.StreamClosedException

class MessageImpl(
  override val type: MessageType,
  initLength: Long,
  val input: AsyncInput,
  private var maskFlag: Boolean,
  private var mask: Int,
  private var lastFrame: Boolean,
) : Message {
  private var inputReady = initLength
  private var closed = false

  private fun checkClosed() {
    if (closed) {
      throw StreamClosedException()
    }
  }

  override suspend fun asyncClose() {
    checkClosed()

    if (inputReady > 0L) {
      copyTo(AsyncOutput.NULL)
    }
    closed = true
  }

  private var cursor = 0L

  val lastPart
    get() = lastFrame

  override val available: Int
    get() =
      when {
        inputReady == 0L && lastFrame -> 0
        inputReady > 0L -> inputReady.toInt()
        else -> -1
      }

  override suspend fun read(dest: ByteBuffer): Int {
    checkClosed()
    if (inputReady == 0L && lastFrame) {
      return 0
    }
    val read = if (maskFlag) {
      val pos1 = dest.position
      val lim1 = dest.limit
      dest.limit = dest.position + minOf(inputReady, dest.remaining.toLong()).toInt()
      val n = input.read(dest)

      dest.position = pos1
      dest.limit = n
      cursor = Message.encode(cursor, mask, dest)
      dest.limit = lim1
      n
    } else {
      val lim1 = dest.limit
      dest.limit = dest.position + minOf(inputReady, dest.remaining.toLong()).toInt()
      val n = input.read(dest)
      dest.limit = lim1
      n
    }
    inputReady -= read.toLong()

    if (inputReady == 0L && !lastFrame) {
      val v = WebSocketHeader()
      WebSocketHeader.read(input, v)
      lastFrame = v.finishFlag
      cursor = 0L
      mask = v.mask
      maskFlag = v.maskFlag
      inputReady = v.length
    }
    return read
  }
}
*/
