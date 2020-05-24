package pw.binom.base64

import pw.binom.io.InputStream
import pw.binom.io.Reader
import kotlin.experimental.or

class Base64DecodeInputStream(val reader: Reader) : InputStream {
    private var counter = 0
    private var value = 0.toByte()

    private fun getChar() = reader.read()

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (offset + length > data.size)
            throw IndexOutOfBoundsException()
        var off = offset
        var len = length

        while (len > 0) {
            val c = getChar() ?: break
            if (c == '=') {
                counter++
                continue
            }
            val value = charFromBase64(c)
            when (counter) {
                0 -> {
                    this.value = this.value or (value shl 2)
                }
                1 -> {
                    val write = this.value or (value shr 4)
                    data[off] = write
                    off++
                    len--
                    this.value = value shl 4
                }
                2 -> {
                    val write = this.value or (value shr 2)
                    data[off] = write
                    off++
                    len--
                    this.value = value shl 6
                }
                3 -> {
                    val write = this.value or (value)
                    data[off] = write
                    off++
                    len--
                    this.value = 0
                }
                else -> throw RuntimeException()
            }
            counter++
            if (counter == 4)
                counter = 0
        }
        return length - len
    }

    override fun close() {

    }

}