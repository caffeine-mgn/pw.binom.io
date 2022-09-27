package pw.binom.io.http.websocket

import pw.binom.EmptyAsyncInput
import pw.binom.NullAsyncOutput
import pw.binom.copyTo
import pw.binom.io.AsyncInput
import pw.binom.io.ByteBuffer
import pw.binom.io.StreamClosedException

internal class MessageImpl2(val onClose: (MessageImpl2) -> Unit) : Message {
    private var inputReady = 0L
    private var closed = false
    private var lastFrame: Boolean = false
    private var maskFlag: Boolean = false
    private var mask: Int = 0
    private var input: AsyncInput = EmptyAsyncInput

    override val available: Int
        get() =
            when {
                inputReady == 0L && lastFrame -> 0
                inputReady > 0L -> inputReady.toInt()
                else -> -1
            }

    override suspend fun read(dest: ByteBuffer): Int {
        checkClosed()
        if (inputReady == 0L && lastFrame) {
            return 0
        }
        val read = if (maskFlag) {
            val pos1 = dest.position
            val lim1 = dest.limit
            dest.limit = dest.position + minOf(inputReady, dest.remaining.toLong()).toInt()
            val n = input.read(dest)

            dest.position = pos1
            dest.limit = n
            cursor = Message.encode(cursor, mask, dest)
            dest.limit = lim1
            n
        } else {
            val lim1 = dest.limit
            dest.limit = dest.position + minOf(inputReady, dest.remaining.toLong()).toInt()
            val n = input.read(dest)
            dest.limit = lim1
            n
        }
        inputReady -= read.toLong()

        if (inputReady == 0L && !lastFrame) {
            val v = WebSocketHeader()
            WebSocketHeader.read(input, v)
            lastFrame = v.finishFlag
            cursor = 0L
            mask = v.mask
            maskFlag = v.maskFlag
            inputReady = v.length
        }
        return read
    }

    private fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }

    override suspend fun asyncClose() {
        checkClosed()

        if (inputReady > 0L) {
            copyTo(NullAsyncOutput)
        }
        input = EmptyAsyncInput
        closed = true
        onClose(this)
    }

    override var type: MessageType = MessageType.CLOSE
    private var cursor = 0L

    fun reset(
        initLength: Long,
        type: MessageType,
        lastFrame: Boolean,
        maskFlag: Boolean,
        mask: Int,
        input: AsyncInput
    ) {
        inputReady = initLength
        this.type = type
        this.lastFrame = lastFrame
        this.maskFlag = maskFlag
        this.mask = mask
        this.input = input
        closed = false
        cursor = 0L
    }
}
