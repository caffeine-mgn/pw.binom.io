package pw.binom.base64

import pw.binom.Output
import pw.binom.io.OutputStream
import pw.binom.io.write
import pw.binom.writeByte
import kotlin.experimental.or

internal fun charFromBase64(value: Char): Byte =
        when (value) {
            in ('A'..'Z') -> (value.toInt() - 'A'.toInt()).toByte()
            in ('a'..'z') -> (26 + value.toInt() - 'a'.toInt()).toByte()
            in ('0'..'9') -> (52 + value.toInt() - '0'.toInt()).toByte()
            '+' -> 62.toByte()
            '/' -> 63.toByte()
            else -> throw IllegalArgumentException("Invalid char [$value] (0x${value.toInt().toString(16)})")
        }

class Base64DecodeAppendable(val stream: Output) : Appendable {

    private var g = 0
    private var b = 0.toByte()

    override fun append(c: Char): Appendable {
        if (c == '=') {
            g++
            return this
        }
        val value = charFromBase64(c)
        when (g) {
            0 -> {
                b = b or (value shl 2)
            }
            1 -> {
                val write = b or (value shr 4)
                stream.writeByte(write)
                b = value shl 4
            }
            2 -> {
                val write = b or (value shr 2)
                stream.writeByte(write)
                b = value shl 6
            }
            3 -> {
                val write = b or (value)
                stream.writeByte(write)
                b = 0
            }
            else -> throw RuntimeException()
        }
        g++
        if (g == 4)
            g = 0
        return this
    }

    override fun append(csq: CharSequence?): Appendable {
        csq ?: throw IllegalArgumentException("Argument is null")
        return append(csq, 0, csq.length)
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): Appendable {
        csq ?: throw IllegalArgumentException("Argument is null")
        (start until end).forEach {
            append(csq[it])
        }
        return this
    }
}