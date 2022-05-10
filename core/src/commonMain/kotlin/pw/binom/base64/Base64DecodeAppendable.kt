package pw.binom.base64

import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.io.Output
import pw.binom.writeByte
import kotlin.experimental.or

internal fun charFromBase64(value: Char): Byte =
    when (value) {
        in ('A'..'Z') -> (value.code - 'A'.code).toByte()
        in ('a'..'z') -> (26 + value.code - 'a'.code).toByte()
        in ('0'..'9') -> (52 + value.code - '0'.code).toByte()
        '+' -> 62.toByte()
        '/' -> 63.toByte()
        else -> throw IllegalArgumentException("Invalid char [$value] (0x${value.code.toString(16)})")
    }

class Base64DecodeAppendable(val stream: Output) : Appendable, Closeable {

    private var g = 0
    private var b = 0.toByte()
    private val buf = ByteBuffer.alloc(1)

    override fun append(value: Char): Appendable {
        if (value == '=') {
            g++
            return this
        }
        val value2 = charFromBase64(value)
        when (g) {
            0 -> {
                b = b or (value2 shl 2)
            }
            1 -> {
                val write = b or (value2 shr 4)
                stream.writeByte(buf, write)
                b = value2 shl 4
            }
            2 -> {
                val write = b or (value2 shr 2)
                stream.writeByte(buf, write)
                b = value2 shl 6
            }
            3 -> {
                val write = b or (value2)
                stream.writeByte(buf, write)
                b = 0
            }
            else -> throw RuntimeException()
        }
        g++
        if (g == 4)
            g = 0
        return this
    }

    override fun append(value: CharSequence?): Appendable {
        value ?: throw IllegalArgumentException("Argument is null")
        return append(value, 0, value.length)
    }

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable {
        value ?: throw IllegalArgumentException("Argument is null")
        (startIndex until endIndex).forEach {
            append(value[it])
        }
        return this
    }

    override fun close() {
        buf.close()
    }
}
