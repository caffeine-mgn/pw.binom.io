package pw.binom.io.http.websocket

import pw.binom.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.ThreadRef
import pw.binom.concurrency.asReference
import pw.binom.io.AsyncChannel
import pw.binom.io.StreamClosedException
import pw.binom.io.use
import pw.binom.network.SocketClosedException
import pw.binom.network.network

abstract class AbstractWebSocketConnection(
    rawConnection: AsyncChannel,
    input: AsyncInput,
    output: AsyncOutput
) : WebSocketConnection {

    private val _output = output.asReference()
    protected val output: AsyncOutput
        get() = _output.value
    private val _input = input.asReference()
    protected val input
        get() = _input.value
//    private val selfRef = this.asReference()

    private var closed by AtomicBoolean(false)
//    private val channel = rawConnection.asReference()
    private val networkThread = ThreadRef()

    var receivedCloseMessage by AtomicBoolean(false)
        private set

    var sentCloseMessage by AtomicBoolean(false)
        private set

    init {
        doFreeze()
    }

    override suspend fun write(type: MessageType, func: suspend (AsyncOutput) -> Unit) {
        checkClosed()
        func.doFreeze()
        if (networkThread.same) {
            write(type).use {
                func(it)
            }
        } else {
            network {
                write(type).use {
                    func(it)
                }
            }
        }
    }

    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    override suspend fun read(): Message {
        requiredNetworkThread()
        checkClosed()
        if (receivedCloseMessage) {
            throw IllegalStateException("Can't read message. Already received close message")
        }
        if (sentCloseMessage) {
            throw IllegalStateException("Can't read message. Already sent close message")
        }
        val header = WebSocketHeader()
        LOOP@ while (true) {
            try {
                WebSocketHeader.read(input, header)
                val type = when (header.opcode) {
                    1.toByte() -> MessageType.TEXT
                    2.toByte() -> MessageType.BINARY
                    8.toByte() -> MessageType.CLOSE
                    else -> {
                        runCatching {
                            closeTcp()
                        }
                        throw WebSocketClosedException(WebSocketClosedException.ABNORMALLY_CLOSE)
                    }
                }
                if (type == MessageType.CLOSE) {
                    this.receivedCloseMessage = true
                }
                return MessageImpl(
                    type = type,
                    input = input,
                    initLength = header.length,
                    mask = header.mask,
                    maskFlag = header.maskFlag,
                    lastFrame = header.finishFlag
                )
            } catch (e: SocketClosedException) {
                kotlin.runCatching {
                    closeTcp()
                }
                throw WebSocketClosedException(WebSocketClosedException.ABNORMALLY_CLOSE)
            }
        }
    }

    override suspend fun write(type: MessageType): AsyncOutput {
        checkClosed()
        requiredNetworkThread()
        if (receivedCloseMessage) {
            throw IllegalStateException("Can't write message. Already received close message")
        }
        if (sentCloseMessage) {
            throw IllegalStateException("Can't write message. Already sent close message")
        }
        if (type == MessageType.CLOSE) {
            sentCloseMessage = true
        }
        return WSOutput(
            messageType = type,
            bufferSize = DEFAULT_BUFFER_SIZE,
            stream = output,
            masked = masking
        )
    }

    protected abstract val masking: Boolean

    private suspend fun closeTcp() {
        if (closed){
            return
        }
        closed = true
//        channel.close()
        _input.close()
        _output.close()
//        selfRef.close()

//        channel.value.asyncClose()
        runCatching { input.asyncClose() }
        runCatching { output.asyncClose() }
    }

    private fun requiredNetworkThread() {
        if (!networkThread.same) {
            throw IllegalStateException("This method must be call from network thread")
        }
    }

    private suspend fun sendFinish(code: Short = 1006, body: ByteBuffer? = null) {
        val v = WebSocketHeader()
        v.opcode = 8
        v.length = Short.SIZE_BYTES.toULong() + (body?.remaining ?: 0).toULong()
        v.maskFlag = masking
        v.finishFlag = true
        WebSocketHeader.write(output, v)
        ByteBuffer.alloc(Short.SIZE_BYTES + (body?.remaining ?: 0)) {
            it.writeShort(code)
            if (body != null) {
                it.write(body)
            }
            it.clear()
            if (masking) {
                Message.encode(v.mask, it)
            }
            output.write(it)
        }
        output.flush()
    }

    suspend fun asyncClose(code: Short, body: ByteBuffer? = null) {
        checkClosed()
        requiredNetworkThread()
        if (this.receivedCloseMessage) {
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
        requiredNetworkThread()
        try {
            if (!this.receivedCloseMessage) {
                sendFinish(code = WebSocketClosedException.CLOSE_NORMAL)
            }
        } finally {
            closeTcp()
        }
    }
}