package pw.binom.io.http.websocket

import pw.binom.*
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.use
import kotlin.experimental.and
import kotlin.experimental.or

class WebSocketHeader {
    var opcode: Byte = 0
    var length = 0L
    var maskFlag = false
    var mask = 0
    var finishFlag = false

    companion object {
        suspend fun read(input: AsyncInput, dest: WebSocketHeader) {
            val buf = ByteBuffer(8)
            val first = input.readByte(buf)
            val second = input.readByte(buf)

            dest.finishFlag = first.toInt() and 0b10000000 != 0
            dest.opcode = first and 0b1111

            dest.length = (second and 0b1111111.toByte()).let {
                when (it) {
                    126.toByte() -> {
                        val s = input.readShort(buf)
//                        println(
//                            "WebSocketHeader:: return size as Short. $s, ${s.toUShort()} ${
//                            s.toUShort().toLong()
//                            } ${s.toLong()}"
//                        )
                        s.toUShort().toLong()
                    }

                    127.toByte() -> {
                        /*println("WebSocketHeader:: return size as Long"); */input.readLong(buf)
                    }

                    else -> {
                        /*println("WebSocketHeader:: return size as is $it ${it.toLong()}"); */it.toLong()
                    }
                }
            }
//            println("WebSocketHeader::read dest.length=${dest.length}")
            dest.maskFlag = second and 0b10000000.toByte() != 0.toByte()

            if (dest.maskFlag) {
                dest.mask = input.readInt(buf)
            }
            buf.close()
        }

        suspend fun write(output: AsyncOutput, src: WebSocketHeader) {
            ByteBuffer(8).use { buf ->
                var value = src.opcode and 0b111
                if (src.finishFlag) {
                    value = value or 0b10000000.toByte()
                }
                output.writeByte(value = value, buffer = buf)

                value = if (src.maskFlag) {
                    0b10000000.toByte()
                } else {
                    0b00000000.toByte()
                }
                when {
                    src.length > Short.MAX_VALUE -> {
                        output.writeByte(value = 127.toByte() or value, buffer = buf)
                        output.writeLong(value = src.length, buffer = buf)
                    }

                    src.length >= 126L -> {
                        output.writeByte(value = 126.toByte() or value, buffer = buf)
                        output.writeShort(value = src.length.toShort(), buffer = buf)
                    }

                    else -> output.writeByte(value = src.length.toByte() or value, buffer = buf)
                }
                if (src.maskFlag) {
                    output.writeInt(value = src.mask, buffer = buf)
                }
            }
        }
    }

    override fun toString(): String =
        "WebSocketHeader(opcode=$opcode, length=$length, maskFlag=$maskFlag, mask=${
        mask.toUInt().toString(2)
        }, finishFlag=$finishFlag)"
}
