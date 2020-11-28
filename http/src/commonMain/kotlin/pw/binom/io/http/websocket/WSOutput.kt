package pw.binom.io.http.websocket

import pw.binom.*
import pw.binom.io.AbstractAsyncBufferedOutput
import pw.binom.io.StreamClosedException
import kotlin.experimental.xor
import kotlin.random.Random

class WSOutput(
        val messageType: MessageType,
        val masked: Boolean,
        override val stream: AsyncOutput,
        bufferSize: Int) : AbstractAsyncBufferedOutput() {
    override val buffer: ByteBuffer = ByteBuffer.alloc(bufferSize).empty()

    private var first = true
    private var eof = false
    private var closed = false

    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    override suspend fun flush() {
//        val masked=false
        checkClosed()
        if (buffer.position > 0) {
            val v = WebSocketHeader()
            v.finishFlag = eof
            val length = buffer.position
            v.maskFlag = masked
            if (masked) {
                val mask = Random.nextInt()
                v.mask = mask
                buffer.flip()
                Message.encode(0uL, mask, buffer)
                buffer.position = length
                buffer.limit = buffer.capacity
            }
            v.length = length.toULong()
            v.opcode = if (!first)
                0
            else
                when (messageType) {
                    MessageType.TEXT -> 1
                    MessageType.BINARY -> 2
                }
            WebSocketHeader.write(stream, v)
            first = false
        }
        super.flush()
    }

    override suspend fun close() {
        checkClosed()
        try {
            val needSendEnd = buffer.position == 0 && !first
            eof = true
            super.close()

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