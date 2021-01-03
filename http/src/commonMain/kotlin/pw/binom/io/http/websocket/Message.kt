package pw.binom.io.http.websocket

import pw.binom.*
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
}

val Message.isCloseMessage
    get() = type == MessageType.CLOSE