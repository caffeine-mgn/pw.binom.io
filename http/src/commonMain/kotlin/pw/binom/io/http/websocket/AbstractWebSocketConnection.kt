package pw.binom.io.http.websocket

import pw.binom.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.asReference
import pw.binom.io.StreamClosedException
import pw.binom.io.socket.SocketClosedException
import pw.binom.io.socket.nio.SocketNIOManager

abstract class AbstractWebSocketConnection(rawConnection: SocketNIOManager.ConnectionRaw, input: AsyncInput, output: AsyncOutput) : WebSocketConnection {

    private val _output = output.asReference()
    protected val output: AsyncOutput
        get() = _output.value
    private val _input = input.asReference()
    protected val input
        get() = _input.value

//    private val header = WebSocketHeader()
    private val selfRef = this.asReference()

    private var closed by AtomicBoolean(false)
    private val holder = rawConnection.holder

    init {
        doFreeze()
    }

    override fun write(type: MessageType, func: suspend (AsyncOutput) -> Unit) {
        checkClosed()
        func.doFreeze()
        holder.waitReadyForWrite {
            async { func(write(type)) }
        }
    }

    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    override suspend fun read(): Message {
        checkClosed()
        val header = WebSocketHeader()
        LOOP@ while (true) {
            try {
                println("read header")
                WebSocketHeader.read(input, header)
                val type = when (header.opcode) {
                    1.toByte() -> MessageType.TEXT
                    2.toByte() -> MessageType.BINARY
                    0.toByte() -> {
                        println("Invalid message type")
                        closeTcp()
                        throw WebSocketClosedException(WebSocketClosedException.ABNORMALLY_CLOSE)
                    }
                    8.toByte() -> {
                        println("Close message type")
                        closeTcp()
                        throw WebSocketClosedException(0)
                    }
                    else -> {
                        TODO("Unknown opcode: ${header.opcode}")
                    }
                }
                println("Return data")
                return MessageImpl(
                        type = type,
                        input = input,
                        initLength = header.length,
                        mask = header.mask,
                        maskFlag = header.maskFlag,
                        lastFrame = header.finishFlag
                )
            } catch (e: SocketClosedException) {
                println("Socket closed!")
                kotlin.runCatching {
                    close()
                }
                throw WebSocketClosedException(WebSocketClosedException.ABNORMALLY_CLOSE)
            }
        }
    }

    override suspend fun write(type: MessageType): AsyncOutput {
        checkClosed()
        return WSOutput(
                messageType = type,
                bufferSize = DEFAULT_BUFFER_SIZE,
                stream = output,
                masked = masking
        )
    }

    protected abstract val masking: Boolean

    private fun closeTcp() {
        println("close tcp!")
        checkClosed()
        closed = true
        holder.close()
    }

    override suspend fun close() {
        try {
            checkClosed()
            val v = WebSocketHeader()
            v.opcode = 8
            v.length = 0uL
            v.maskFlag = masking
            v.finishFlag = true
            WebSocketHeader.write(output, v)
            selfRef.close()
            output.flush()
        } finally {
            println("Close WS")
            closeTcp()
        }
    }
}