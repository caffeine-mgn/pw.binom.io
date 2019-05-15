package pw.binom

import pw.binom.io.OutputStream
import pw.binom.io.write
import kotlin.experimental.or

internal fun charFromBase64(value: Char): Byte =
        when (value) {
            in ('A'..'Z') -> (value.toInt() - 'A'.toInt()).toByte()
            in ('a'..'z') -> (26 + value.toInt() - 'a'.toInt()).toByte()
            in ('0'..'9') -> (52 + value.toInt() - '0'.toInt()).toByte()
            '+' -> 62.toByte()
            '/' -> 63.toByte()
            else -> throw IllegalArgumentException()
        }

class Base64DecodeAppendable(val stream: OutputStream) : Appendable {

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
                stream.write(write)
                b = value shl 4
            }
            2 -> {
                val write = b or (value shr 2)
                stream.write(write)
                b = value shl 6
            }
            3 -> {
                val write = b or (value)
                stream.write(write)
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
        return append(csq, 0, csq.length - 1)
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): Appendable {
        csq ?: throw IllegalArgumentException("Argument is null")
        (start until end).forEach {
            append(csq[it])
        }
        return this
    }
}

fun Byte.toBinary(max: Int = 6): String {
    val ss = this.toString(2)
    val sb = StringBuilder()
    while (sb.length + ss.length < max) {
        sb.append("0")
    }
    sb.append(ss)
    return sb.toString()
}