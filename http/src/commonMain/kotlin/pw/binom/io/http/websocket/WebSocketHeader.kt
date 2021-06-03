package pw.binom.io.http.websocket

import pw.binom.*
import pw.binom.io.use
import kotlin.experimental.and
import kotlin.experimental.or

class WebSocketHeader {
    var opcode: Byte = 0
    var length = 0uL
    var maskFlag = false
    var mask = 0
    var finishFlag = false

    companion object {
        suspend fun read(input: AsyncInput, dest: WebSocketHeader) {
            val buf = ByteBuffer.alloc(8)
            val first = input.readByte(buf)
            val second = input.readByte(buf)

            dest.finishFlag = first.toInt() and 0b10000000 != 0
            dest.opcode = first and 0b1111

            dest.length = (second and 0b1111111.toByte()).let {
                when (it) {
                    126.toByte() -> input.readShort(buf).toULong()
                    127.toByte() -> input.readLong(buf).toULong()
                    else -> it.toULong()
                }
            }
            dest.maskFlag = second and 0b10000000.toByte() != 0.toByte()

            if (dest.maskFlag) {
                dest.mask = input.readInt(buf)
            }
            buf.close()
        }

        suspend fun write(output: AsyncOutput, src: WebSocketHeader) {
            ByteBuffer.alloc(8) { buf ->
                var value = src.opcode and 0b111
                if (src.finishFlag) {
                    value = value or 0b10000000.toByte()
                }
                output.writeByte(buf, value)

                value = if (src.maskFlag)
                    0b10000000.toByte()
                else
                    0b00000000.toByte()
                when {
                    src.length > UShort.MAX_VALUE -> {
                        output.writeByte(buf, 127.toByte() or value)
                        output.writeLong(buf, src.length.toLong())
                    }
                    src.length >= 126uL -> {
                        output.writeByte(buf, 126.toByte() or value)
                        output.writeShort(buf, src.length.toShort())
                    }
                    else -> output.writeByte(buf, src.length.toByte() or value)
                }
                if (src.maskFlag)
                    output.writeInt(buf, src.mask)
            }
        }
    }

    override fun toString(): String =
        "WebSocketHeader(opcode=$opcode, length=$length, maskFlag=$maskFlag, mask=${
            mask.toUInt().toString(2)
        }, finishFlag=$finishFlag)"


}