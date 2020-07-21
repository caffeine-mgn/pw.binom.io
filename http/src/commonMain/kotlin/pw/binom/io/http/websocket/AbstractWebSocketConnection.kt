package pw.binom.io.http.websocket

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.StreamClosedException
import pw.binom.io.http.websocket.*

abstract class AbstractWebSocketConnection(val input: AsyncInput, val output: AsyncOutput) : WebSocketConnection {

    private val header = WebSocketHeader()

    private var closed = false

    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    override suspend fun read(): Message {
        checkClosed()
        LOOP@ while (true) {
            WebSocketHeader.read(input, header)
            val type = when (header.opcode) {
                1.toByte() -> MessageType.TEXT
                2.toByte() -> MessageType.BINARY
                0.toByte(), 8.toByte() -> throw WebSocketClosedException()
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

    protected abstract val masking:Boolean

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