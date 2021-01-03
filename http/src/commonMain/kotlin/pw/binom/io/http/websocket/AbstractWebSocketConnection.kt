package pw.binom.io.http.websocket

import pw.binom.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.ThreadRef
import pw.binom.concurrency.asReference
import pw.binom.io.StreamClosedException
import pw.binom.io.use
import pw.binom.network.SocketClosedException
import pw.binom.network.TcpConnection

abstract class AbstractWebSocketConnection(
    rawConnection: TcpConnection,
    input: AsyncInput,
    output: AsyncOutput
) : WebSocketConnection {

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
    private val channel = rawConnection.channel
    private val networkThread = ThreadRef()

    init {
        doFreeze()
    }

    override fun write(type: MessageType, func: suspend (AsyncOutput) -> Unit) {
        checkClosed()
        func.doFreeze()

        if (networkThread.same) {
            async {
                write(type).use {
                    func(it)
                }
            }
        } else {
            holder.waitReadyForWrite {
                async { func(write(type)) }
            }
        }
    }

    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    override suspend fun read(): Message {
        if (!networkThread.same) {
            throw IllegalStateException("This method must be call from network thread")
        }
        checkClosed()
        val header = WebSocketHeader()
        LOOP@ while (true) {
            try {
                WebSocketHeader.read(input, header)
                val type = when (header.opcode) {
                    1.toByte() -> MessageType.TEXT
                    2.toByte() -> MessageType.BINARY
                    8.toByte() -> MessageType.CLOSE
//                    8.toByte() -> {
//                        kotlin.runCatching {
//                            closeTcp()
//                        }
//                        throw WebSocketClosedException(0)
//                    }
                    else -> {
                        kotlin.runCatching {
                            closeTcp()
                        }
                        throw WebSocketClosedException(WebSocketClosedException.ABNORMALLY_CLOSE)
                    }
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
        if (!networkThread.same) {
            throw IllegalStateException("This method must be call from network thread")
        }
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
        checkClosed()
        closed = true
        holder.key.close()
        channel.close()
    }

    override suspend fun asyncClose() {
        if (!networkThread.same) {
            throw IllegalStateException("This method must be call from network thread")
        }
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
            closeTcp()
        }
    }
}