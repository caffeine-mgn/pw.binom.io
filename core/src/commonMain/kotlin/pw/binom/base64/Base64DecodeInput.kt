package pw.binom.base64

import pw.binom.io.ByteBuffer
import pw.binom.io.Input
import pw.binom.io.Reader
import kotlin.experimental.or

class Base64DecodeInput(val reader: Reader) : Input {
    private var counter = 0
    private var value = 0.toByte()

    private var buffer = CharArray(32)
    private var cursor = buffer.size
    private var len = buffer.size

    private fun getChar(): Char? {
        if (cursor >= len) {
            len = reader.read(buffer)
            if (len == 0)
                return null
            cursor = 0
        }
        return buffer[cursor++]
    }

    override fun read(dest: ByteBuffer): Int {
        val length = dest.remaining123

        while (dest.remaining123 > 0) {
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
                    dest.put(write)
                    this.value = value shl 4
                }
                2 -> {
                    val write = this.value or (value shr 2)
                    dest.put(write)
                    this.value = value shl 6
                }
                3 -> {
                    val write = this.value or (value)
                    dest.put(write)
                    this.value = 0
                }
                else -> throw RuntimeException()
            }
            counter++
            if (counter == 4)
                counter = 0
        }
        return length - dest.remaining123
    }

    override fun close() {
        // Do nothing
    }
}
