package pw.binom.io.http.websocket

import pw.binom.NullAsyncOutput
import pw.binom.copyTo
import pw.binom.io.AsyncInput
import pw.binom.io.ByteBuffer
import pw.binom.io.StreamClosedException

class MessageImpl(
    override val type: MessageType,
    initLength: ULong,
    val input: AsyncInput,
    private var maskFlag: Boolean,
    private var mask: Int,
    private var lastFrame: Boolean
) : Message {
    private var inputReady = initLength
    private var closed = false

    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    override suspend fun asyncClose() {
        checkClosed()

        if (inputReady > 0uL) {
            copyTo(NullAsyncOutput)
        }
        closed = true
    }

    private var cursor = 0uL

    val lastPart
        get() = lastFrame

    override val available: Int
        get() =
            when {
                inputReady == 0uL && lastFrame -> 0
                inputReady > 0uL -> inputReady.toInt()
                else -> -1
            }

    override suspend fun read(dest: ByteBuffer): Int {
        checkClosed()
        if (inputReady == 0uL && lastFrame)
            return 0
        val read = if (maskFlag) {
            val pos1 = dest.position
            val lim1 = dest.limit
            dest.limit = dest.position + minOf(inputReady, dest.remaining.toULong()).toInt()
            val n = input.read(dest)

            dest.position = pos1
            dest.limit = n
            cursor = Message.encode(cursor, mask, dest)
            dest.limit = lim1
            n
        } else {
            val lim1 = dest.limit
            dest.limit = dest.position + minOf(inputReady, dest.remaining.toULong()).toInt()
            val n = input.read(dest)
            dest.limit = lim1
            n
        }
        inputReady -= read.toULong()

        if (inputReady == 0uL && !lastFrame) {
            val v = WebSocketHeader()
            WebSocketHeader.read(input, v)
            lastFrame = v.finishFlag
            cursor = 0uL
            mask = v.mask
            maskFlag = v.maskFlag
            inputReady = v.length
        }
        return read
    }
}
