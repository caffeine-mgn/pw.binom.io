package pw.binom.io.http.websocket

import pw.binom.*
import pw.binom.io.use
import kotlin.experimental.xor

interface Message : AsyncInput {
    val type: MessageType

    companion object {

        fun encode(mask: Int, data: ByteBuffer) {
            val length = data.position
            data.flip()
            encode(0uL, mask, data)
            data.position = length
            data.limit = data.capacity
        }

        fun encode(cursor: ULong, mask: Int, data: ByteBuffer): ULong {
            var cursorLocal = cursor
            data.forEachIndexed { _, byte ->
                data.put(byte xor mask[(cursorLocal and 0x03uL).toInt()])
                cursorLocal++
            }
            return cursorLocal
        }
    }

    val isCloseMessage
        get() = type == MessageType.CLOSE
}