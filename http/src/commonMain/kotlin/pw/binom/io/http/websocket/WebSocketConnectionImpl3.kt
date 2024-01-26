@file:Suppress("ktlint:standard:no-wildcard-imports")

package pw.binom.io.http.websocket

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.atomic.AtomicBoolean
import pw.binom.coroutines.SimpleAsyncLock
import pw.binom.io.*
import pw.binom.network.SocketClosedException
import pw.binom.writeShort

class WebSocketConnectionImpl3(
  private var _output: AsyncOutput,
  private var _input: AsyncInput,
  private var masking: Boolean,
  private val mainChannel: AsyncCloseable,
  private val bufferSize: Int = DEFAULT_BUFFER_SIZE,
) : WebSocketConnection {
  private val closed = AtomicBoolean(false)

  private val receivedCloseMessage = AtomicBoolean(false)
  private val sentCloseMessage = AtomicBoolean(false)

  override val isReadReady: Boolean
    get() = readChannelLock.isLocked
  override val isWriteReady: Boolean
    get() = writeChannelLock.isLocked

  private val readChannelLock = SimpleAsyncLock()
  private val writeChannelLock = SimpleAsyncLock()

  //  private val header = WebSocketHeader()
  private val message = MessageImpl3(input = _input)

  private fun checkClosed() {
    if (closed.getValue()) {
      throw StreamClosedException()
    }
  }

  private suspend fun sendFinish(
    code: Short = 1006,
    body: ByteBuffer? = null,
  ) {
    val v = WebSocketHeader()
    v.opcode = Opcode.CLOSE
    v.length = Short.SIZE_BYTES.toLong() + (body?.remaining ?: 0).toLong()
    v.maskFlag = masking
    v.finishFlag = true
    WebSocketHeader.write(_output, v)
    ByteBuffer(Short.SIZE_BYTES + (body?.remaining ?: 0)).use {
      it.writeShort(code)
      if (body != null) {
        it.write(body)
      }
      it.clear()
      if (masking) {
        Message.encode(v.mask, it)
      }
      _output.write(it)
    }
    _output.flush()
  }

  override suspend fun read(): Message {
    checkClosed()
    if (receivedCloseMessage.getValue()) {
      throw IllegalStateException("Can't read message. Already received close message")
    }
    if (sentCloseMessage.getValue()) {
      throw IllegalStateException("Can't read message. Already sent close message")
    }
    LOOP@ while (true) {
      try {
        readChannelLock.synchronize {
//          WebSocketHeader.read(input = _input, dest = header)
          try {
            message.startMessage()
          } catch (e: Throwable) {
            runCatching {
              closeTcp()
            }
            throw e
          }
          val type = message.type
          if (type == MessageType.CLOSE) {
            this.receivedCloseMessage.setValue(true)
          }
        }
        return message
      } catch (e: SocketClosedException) {
        runCatching {
          closeTcp()
        }
        throw WebSocketClosedException(
          connection = this,
          code = WebSocketClosedException.ABNORMALLY_CLOSE,
        )
      }
    }
  }

  private fun checkState() {
    if (receivedCloseMessage.getValue()) {
      throw IllegalStateException("Can't write message. Already received close message")
    }
    if (sentCloseMessage.getValue()) {
      throw IllegalStateException("Can't write message. Already sent close message")
    }
  }

  override suspend fun write(type: MessageType): AsyncOutput {
    checkClosed()
    checkState()
    writeChannelLock.lock()
    if (type == MessageType.CLOSE) {
      sentCloseMessage.setValue(true)
    }
    return WebSocketOutput(
      messageType = type,
      bufferSize = bufferSize,
      stream = _output,
      masked = masking,
      writeLock = writeChannelLock,
    )
  }

  override suspend fun writeIfReady(type: MessageType): AsyncOutput? {
    checkClosed()
    checkState()

    if (!writeChannelLock.tryLock()) {
      return null
    }
    if (type == MessageType.CLOSE) {
      sentCloseMessage.setValue(true)
    }
    return WebSocketOutput(
      messageType = type,
      bufferSize = bufferSize,
      stream = _output,
      masked = masking,
      writeLock = writeChannelLock,
    )
  }

  private suspend fun closeTcp() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    _input.asyncCloseAnyway()
    _output.asyncCloseAnyway()
    mainChannel.asyncCloseAnyway()
  }

  suspend fun asyncClose(
    code: Short,
    body: ByteBuffer? = null,
  ) {
    checkClosed()
    if (this.receivedCloseMessage.getValue()) {
      throw IllegalStateException("Can't send close message because already got close message")
    }
    try {
      sendFinish(code = code, body = body)
    } finally {
      closeTcp()
    }
  }

  override suspend fun asyncClose() {
    checkClosed()
    try {
      if (!this.receivedCloseMessage.getValue()) {
        sendFinish(code = WebSocketClosedException.CLOSE_NORMAL)
      }
    } finally {
      closeTcp()
    }
  }
}
