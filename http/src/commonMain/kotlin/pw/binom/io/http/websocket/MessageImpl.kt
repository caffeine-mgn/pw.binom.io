package pw.binom.io.http.websocket

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.forEach
import pw.binom.get
import pw.binom.io.StreamClosedException
import kotlin.experimental.xor

class MessageImpl(override val type: MessageType,
                  initLength: ULong,
                  val input: AsyncInput,
                  private var maskFlag: Boolean,
                  private var mask: Int,
                  private var lastFrame:Boolean
) : Message {
    private var inputReady = initLength
    private var closed = false

    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    override suspend fun close() {
        checkClosed()
        if (inputReady > 0uL)
            throw IllegalStateException("Current block is't finished. Remaining: [$inputReady]")
        closed = true
    }

    private var cursor = 0uL

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
            cursor=Message.encode(cursor,mask, dest)
//            dest.forEach { byte ->
//                val b = byte xor mask[(cursor and 0x03uL).toInt()]
//                cursor++
//                dest.put(b)
//            }
            dest.limit = lim1
            n
        } else {
            input.read(dest)
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