package pw.binom.io.http.websocket

import pw.binom.io.*
import kotlin.random.Random

class WSOutput(
    val messageType: MessageType,
    val masked: Boolean,
    override val stream: AsyncOutput,
    bufferSize: Int
) : AbstractAsyncBufferedOutput() {
    override val buffer: ByteBuffer = ByteBuffer.alloc(bufferSize).empty()

    private var first = true
    private var eof = false
    private var closed = false

    private fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    override suspend fun flush() {
        checkClosed()
        if (buffer.position > 0) {
            val v = WebSocketHeader()
            v.finishFlag = eof
            val length = buffer.position
            v.maskFlag = masked
            if (masked) {
                v.mask = Random.nextInt()
                Message.encode(v.mask, buffer)
            }
            v.length = length.toULong()
            v.opcode = if (!first)
                0
            else
                when (messageType) {
                    MessageType.TEXT -> 1
                    MessageType.BINARY -> 2
                    MessageType.CLOSE -> 8
                }
            WebSocketHeader.write(stream, v)
            first = false
        }
        super.flush()
    }

    override suspend fun asyncClose() {
        checkClosed()
        try {
            flush()
            val needSendEnd = buffer.position == 0 && !first
            eof = true
            super.asyncClose()

            if (needSendEnd) {
                val v = WebSocketHeader()
                v.opcode = 0
                v.length = 0uL
                v.maskFlag = masked
                if (masked)
                    v.mask = Random.nextInt()
                v.finishFlag = true
                WebSocketHeader.write(stream, v)
                stream.flush()
            }
        } finally {
            buffer.close()
            closed = true
        }
    }
}
