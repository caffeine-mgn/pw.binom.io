package pw.binom.io.http.websocket

import pw.binom.*
import pw.binom.io.use
import kotlin.experimental.xor

interface Message : AsyncInput {
    val type: MessageType

    companion object {
        fun encode(cursor: ULong, mask: Int, data: ByteBuffer): ULong {
            var cursorLocal = cursor
            data.forEachIndexed { i, byte ->
                data.put(byte xor mask[(cursorLocal and 0x03uL).toInt()])
                cursorLocal++
            }
            return cursorLocal
        }
    }

    suspend fun readCloseCode(): Short {
        if (type != MessageType.CLOSE) {
            throw IllegalStateException("Message is not close type")
        }
        return ByteBuffer.alloc(Short.SIZE_BITS).use { buf ->
            read(buf)
            buf.flip()
            buf.readShort()
        }
    }
}

val Message.isCloseMessage
    get() = type == MessageType.CLOSE