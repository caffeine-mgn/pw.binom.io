package pw.binom.io.http.websocket

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.async
import pw.binom.io.StreamClosedException
import pw.binom.io.http.websocket.*
import pw.binom.io.socket.SocketClosedException
import pw.binom.io.socket.nio.SocketNIOManager

abstract class AbstractWebSocketConnection(val rawConnection: SocketNIOManager.ConnectionRaw, val input: AsyncInput, val output: AsyncOutput) : WebSocketConnection {

    private val header = WebSocketHeader()

    private var closed = false
    private lateinit var listener: (SocketNIOManager.ConnectionRaw) -> Unit
    override var incomeMessageListener: (suspend (WebSocketConnection) -> Unit)? = null
        set(value) {
            field = value
            if (value == null) {
                rawConnection.waitReadyForRead(null)
            } else {
                rawConnection.waitReadyForRead(listener)
            }
        }

    init {
        listener = {
            async {
                try {
                    val func = incomeMessageListener
                    if (func != null) {
                        func.invoke(this)
                        rawConnection.waitReadyForRead(listener)
                    }
                } catch (e: Throwable) {
                    rawConnection.detach().close()
                }
            }
        }
    }

    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    override suspend fun read(): Message {
        checkClosed()
        LOOP@ while (true) {
            try {
                WebSocketHeader.read(input, header)
                println("Header $header")
                val type = when (header.opcode) {
                    1.toByte() -> MessageType.TEXT
                    2.toByte() -> MessageType.BINARY
                    0.toByte() -> throw WebSocketClosedException(WebSocketClosedException.ABNORMALLY_CLOSE)
                    8.toByte() -> throw WebSocketClosedException(0)
                    else -> TODO("Unknown opcode: ${header.opcode}")
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

    override suspend fun close() {
        checkClosed()
        val v = WebSocketHeader()
        v.opcode = 8
        v.length = 0uL
        v.maskFlag = masking
        v.finishFlag = true
        WebSocketHeader.write(output, v)
        output.flush()
        closed = true
    }
}