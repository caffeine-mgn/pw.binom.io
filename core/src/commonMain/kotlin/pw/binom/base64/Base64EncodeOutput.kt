package pw.binom.base64

import pw.binom.ByteBuffer
import pw.binom.Output
import pw.binom.io.StreamClosedException
import kotlin.experimental.and
import kotlin.experimental.or


internal fun byteToBase64(value: Byte): Char =
    when (value) {
        in (0..25) -> ('A'.toInt() + value).toChar()
        in (26..51) -> ('a'.toInt() + value - 26).toChar()
        in (52..61) -> ('0'.toInt() + value - 52).toChar()
        62.toByte() -> '+'
        63.toByte() -> '/'
        else -> throw IllegalArgumentException()
    }

class Base64EncodeOutput(private val appendable: Appendable) : Output {


    private var old = 0.toByte()
    private var counter = 0

    private fun write(data: Byte) {
        checkClosed()
        when (counter) {
            0 -> {
                val ff = data shr 2
                old = ((data and 3) shl 4)
                appendable.append(byteToBase64(ff))
            }
            1 -> {
                val ff = old or (data shr 4)
                old = ((data and 15) shl 2)
                appendable.append(byteToBase64(ff))
            }
            2 -> {
                val ff = old or (data shr 6)
                old = 0
                appendable.append(byteToBase64(ff))
                appendable.append(byteToBase64((data and 63)))
            }
        }
        counter++
        if (counter == 3)
            counter = 0
    }

    private var closed = false
    private fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }

    override fun write(data: ByteBuffer): Int {
        checkClosed()
        val length = data.remaining
        (data.position until data.limit).forEach {
            write(data[it])
        }
        return length
    }

    override fun flush() {
        checkClosed()
    }

    override fun close() {
        checkClosed()
        closed = true
        when (counter) {
            1 -> appendable.append(byteToBase64(old)).append("==")
            2 -> appendable.append(byteToBase64(old)).append("=")
        }
    }

}