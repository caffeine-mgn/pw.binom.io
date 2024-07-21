package pw.binom.io.http.websocket

import pw.binom.atomic.AtomicBoolean
import pw.binom.io.*

internal class WebSocketInputImpl(
  val input: AsyncInput,
  val connection: WebSocketConnectionImpl,
) : WebSocketInput {
  private var inputReady = 0L
  private val closed = AtomicBoolean(false)
  private val lastFrame: Boolean
    get() = header.finishFlag
  private val maskFlag: Boolean
    get() = header.maskFlag
  private val mask: Int
    get() = header.mask
  override var type: MessageType = MessageType.CLOSE
  private var cursor = 0L
  private val header = WebSocketHeader()

  override val available: Int
    get() =
      when {
        inputReady == 0L && lastFrame -> 0
        inputReady > 0L -> inputReady.toInt()
        else -> -1
      }

  override suspend fun read(dest: ByteBuffer): DataTransferSize {
    checkClosed()
    return readInternal(dest)
  }

  private suspend fun readInternal(dest: ByteBuffer): DataTransferSize {
    if (dest.remaining <= 0) {
      return DataTransferSize.EMPTY
    }
    var wasRead = 0
    while (true) {
      if (inputReady == 0L) {
        if (lastFrame) {
          return DataTransferSize.EMPTY
        }
        WebSocketHeader.read(input, header)
        if (header.opcode != Opcode.CONTINUATION) {
          throw IOException("Invalid opcode ${header.opcode} (${header.opcode.raw})")
        }
        cursor = 0L
        inputReady = header.length
        continue
      }
      val startLimit = dest.limit
      val startPosition = dest.position
      dest.limit = dest.position + minOf(inputReady, dest.remaining.toLong()).toInt()

      val n = input.read(dest)
      if (n.isNotAvailable) {
        return DataTransferSize.ofSize(wasRead)
      }
      wasRead += n.length
      inputReady -= n.length

      if (maskFlag) {
        dest.reset(
          position = startPosition,
          length = n.length,
        )
        cursor = WebSocketInput.encode(
          cursor = cursor,
          mask = mask,
          data = dest,
        )
      }

      dest.limit = startLimit
      dest.position = startPosition + n.length
      return n
    }

    /*
        checkClosed()
        if (inputReady == 0L && lastFrame) {
          return 0
        }
        val lim1 = dest.limit
        dest.limit = dest.position + minOf(inputReady, dest.remaining.toLong()).toInt()
        val read = if (maskFlag) {
          val pos1 = dest.position
          val n = input.read(dest)

          if (n > 0) {
            dest.position = pos1
            dest.limit = n
            cursor = Message.encode(cursor, mask, dest)
            dest.limit = lim1
          }
          n
        } else {
          val n = input.read(dest)
          dest.limit = lim1
          n
        }
        inputReady -= read.toLong()

        if (inputReady == 0L && !lastFrame) {
          WebSocketHeader.read(input, header)
          cursor = 0L
          inputReady = header.length
        }
        return read
        */
  }

  private fun checkClosed() {
    if (closed.getValue()) {
      throw StreamClosedException()
    }
  }

  override suspend fun asyncClose() {
    if (!closed.compareAndSet(false, true)) {
      return
    }

    try {
      if (inputReady > 0L || !lastFrame) {
        // reading also data in message if exist
        ByteBuffer(1024).use { buffer ->
          while (true) {
            buffer.clear()
            if (readInternal(buffer).isNotAvailable) {
              break
            }
          }
        }
      }
    } finally {
      connection.readingMessageFinished()
    }
  }

  suspend fun startMessage() {
    WebSocketHeader.read(input = input, dest = header)
    type = header.opcode.toMessageType()
    inputReady = header.length
    closed.setValue(false)
    cursor = 0L
  }
}
