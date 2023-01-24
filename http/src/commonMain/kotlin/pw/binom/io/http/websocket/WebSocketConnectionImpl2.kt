package pw.binom.io.http.websocket

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.EmptyAsyncInput
import pw.binom.NullAsyncOutput
import pw.binom.atomic.AtomicBoolean
import pw.binom.coroutines.SimpleAsyncLock
import pw.binom.io.*
import pw.binom.network.SocketClosedException
import pw.binom.pool.ObjectFactory
import pw.binom.pool.ObjectPool
import pw.binom.writeShort

class WebSocketConnectionImpl2(val onClose: (WebSocketConnectionImpl2) -> Unit) :
    WebSocketConnection {

    companion object {
        val factory = object : ObjectFactory<WebSocketConnectionImpl2> {
            override fun allocate(pool: ObjectPool<WebSocketConnectionImpl2>): WebSocketConnectionImpl2 =
                WebSocketConnectionImpl2 { self -> pool.recycle(self) }

            override fun deallocate(value: WebSocketConnectionImpl2, pool: ObjectPool<WebSocketConnectionImpl2>) {
                // Do nothing
            }
        }
    }

    private var _output: AsyncOutput = NullAsyncOutput
    private var _input: AsyncInput = EmptyAsyncInput
    private var masking: Boolean = false
    private val readChannelLock = SimpleAsyncLock()
    private val writeChannelLock = SimpleAsyncLock()

    fun reset(
        input: AsyncInput,
        output: AsyncOutput,
        masking: Boolean,
    ) {
        _input = input
        _output = output
        this.masking = masking
        closed.setValue(false)
        receivedCloseMessage.setValue(false)
        sentCloseMessage.setValue(false)
    }

    private var closed = AtomicBoolean(false)
    var receivedCloseMessage = AtomicBoolean(false)
        private set
    var sentCloseMessage = AtomicBoolean(false)
        private set

    private val header = WebSocketHeader()

    private fun checkClosed() {
        if (closed.getValue()) {
            throw StreamClosedException()
        }
    }

    private val message = MessageImpl3()

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
                    WebSocketHeader.read(input = _input, dest = header)
                    val type = when (header.opcode) {
                        1.toByte() -> MessageType.TEXT
                        2.toByte() -> MessageType.BINARY
                        8.toByte() -> MessageType.CLOSE
                        else -> {
                            runCatching {
                                closeTcp()
                            }
                            throw WebSocketClosedException(
                                connection = this,
                                code = WebSocketClosedException.ABNORMALLY_CLOSE,
                            )
                        }
                    }
                    if (type == MessageType.CLOSE) {
                        this.receivedCloseMessage.setValue(true)
                    }
                    message.reset(
                        initLength = header.length,
                        type = type,
                        mask = header.mask,
                        maskFlag = header.maskFlag,
                        lastFrame = header.finishFlag,
                        input = _input
                    )
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

    override suspend fun write(type: MessageType): AsyncOutput {
        checkClosed()
        if (receivedCloseMessage.getValue()) {
            throw IllegalStateException("Can't write message. Already received close message")
        }
        if (sentCloseMessage.getValue()) {
            throw IllegalStateException("Can't write message. Already sent close message")
        }
        if (type == MessageType.CLOSE) {
            sentCloseMessage.setValue(true)
        }
        writeChannelLock.lock()
        return WebSocketOutput(
            messageType = type,
            bufferSize = DEFAULT_BUFFER_SIZE,
            stream = _output,
            masked = masking,
            writeLock = writeChannelLock,
        )
    }

    private suspend fun closeTcp() {
        if (closed.getValue()) {
            return
        }
        closed.setValue(true)
        _input.asyncCloseAnyway()
        _output.asyncCloseAnyway()
        onClose(this)
    }

    private suspend fun sendFinish(code: Short = 1006, body: ByteBuffer? = null) {
        val v = WebSocketHeader()
        v.opcode = 8
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

    suspend fun asyncClose(code: Short, body: ByteBuffer? = null) {
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
