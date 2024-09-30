@file:Suppress("ktlint:standard:no-wildcard-imports")

package pw.binom.io.http.websocket

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.InternalLog
import pw.binom.atomic.AtomicBoolean
import pw.binom.coroutines.SimpleAsyncLock
import pw.binom.io.*
import pw.binom.network.ChannelClosedException
import pw.binom.writeShort

class WebSocketConnectionImpl(
  private var _output: AsyncOutput,
  private var _input: AsyncInput,
  private var masking: Boolean,
  private val mainChannel: AsyncCloseable,
  private val bufferSize: Int = DEFAULT_BUFFER_SIZE,
) : WebSocketConnection {
  private val logger = InternalLog
    .file("WebSocketConnectionImpl")
    .prefix { "this=${hashCode()} " }
  private val closing = AtomicBoolean(false)

  private val receivedCloseMessage = AtomicBoolean(false)
  private val sentCloseMessage = AtomicBoolean(false)

  override val isReadReady: Boolean
    get() = readChannelLock.isLocked
  override val isWriteReady: Boolean
    get() = writeChannelLock.isLocked

  private val readChannelLock = SimpleAsyncLock()
  private val writeChannelLock = SimpleAsyncLock()

  //  private val header = WebSocketHeader()
  private val message = WebSocketInputImpl(input = _input, connection = this)
  private var writing: WebSocketOutput? = null

  override fun toString() =
    "WebSocketConnectionImpl"

  private fun checkClosed() {
    if (closing.getValue()) {
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
        WebSocketInput.encode(v.mask, it)
      }
      _output.write(it)
    }
    _output.flush()
  }

  override suspend fun read(): WebSocketInput {
    checkClosed()
    if (receivedCloseMessage.getValue()) {
      logger.info(method = "read") { "Can't read message because connection already received close flag" }
      throw IllegalStateException("Can't read message. Already received close message")
    }
    if (sentCloseMessage.getValue()) {
      logger.info(method = "read") { "Can't read message because connection already sent close flag" }
      throw IllegalStateException("Can't read message. Already sent close message")
    }
    try {
      logger.info(method = "read") { "Try lock for reading" }
      readChannelLock.lock()
      logger.info(method = "read") { "Lock for reading done. Try to read message header" }
      message.startMessage()
      val type = message.type
      logger.info(method = "read") { "Message header was read. Message type: $type" }
      if (type == MessageType.CLOSE) {
        this.receivedCloseMessage.setValue(true)
      }
      logger.info(method = "read") { "Message read for read. Return it" }
      return message
    } catch (e: ChannelClosedException) {
      logger.info(method = "read") { "Channel closed exception $e" }
      readChannelLock.unlock()
      runCatching {
        closeTcp()
      }
      throw WebSocketClosedException(
        connection = this,
      )
    } catch (e: Throwable) {
      logger.info(method = "read") { "Unknown exception $e" }
      readChannelLock.unlock()
      throw e
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

  internal fun writingMessageFinished() {
    writing = null
    logger.info(method = "writingMessageFinished") { "release writeChannelLock" }
    writeChannelLock.unlock()
  }

  internal fun readingMessageFinished() {
    logger.info(method = "readingMessageFinished") { "release readChannelLock" }
    readChannelLock.unlock()
  }

  override suspend fun write(type: MessageType): AsyncOutput {
    checkClosed()
    checkState()
    writeChannelLock.lock()
    if (type == MessageType.CLOSE) {
      sentCloseMessage.setValue(true)
    }
    val out = WebSocketOutput(
      messageType = type,
      bufferSize = bufferSize,
      stream = _output,
      masked = masking,
      connection = this,
    )
    writing = out
    return out
  }

  override suspend fun writeIfReady(type: MessageType): AsyncOutput? {
    checkClosed()
    checkState()

//    if (!writeChannelLock.tryLock()) {
//      return null
//    }
    if (type == MessageType.CLOSE) {
      sentCloseMessage.setValue(true)
    }
    return WebSocketOutput(
      messageType = type,
      bufferSize = bufferSize,
      stream = _output,
      masked = masking,
      connection = this,
    )
  }

  private suspend fun closeTcp() {
    _input.asyncCloseAnyway()
    _output.asyncCloseAnyway()
    mainChannel.asyncCloseAnyway()
  }

  internal fun tcpClosed() {

  }

  suspend fun sendClose(
    code: Short,
    body: ByteBuffer? = null,
  ) {
    if (!closing.compareAndSet(false, true)) {
      return
    }
    if (this.receivedCloseMessage.getValue()) {
      throw IllegalStateException("Can't send close message because already got close message")
    }
    try {
      write(MessageType.CLOSE).useAsync { msg ->
        ByteBuffer(Short.SIZE_BYTES).use { buffer ->
          buffer.writeShort(code)
          msg.writeFully(buffer)
        }
        if (body != null) {
          msg.writeFully(body)
        }
      }
    } finally {
      internalClose()
    }
  }

  private suspend fun internalClose() {
    val ex = WebSocketClosedException(this)
    readChannelLock.throwAll(ex)
    writeChannelLock.throwAll(ex)
    closeTcp()
  }

  override suspend fun asyncClose() {
    if (!closing.compareAndSet(false, true)) {
      return
    }
    internalClose()
  }
}
