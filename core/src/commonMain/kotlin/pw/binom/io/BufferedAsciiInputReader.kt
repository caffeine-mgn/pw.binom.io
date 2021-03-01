package pw.binom.io

import pw.binom.*

class BufferedAsciiInputReader(
    val input: Input,
    val bufferSize: Int = DEFAULT_BUFFER_SIZE,
) : Reader, Input {
    init {
        require(bufferSize > 4)
    }

    fun reset() {
        buffer.empty()
    }

    private val buffer = ByteBuffer.alloc(bufferSize).empty()

    val available: Int
        get() = if (buffer.remaining > 0) buffer.remaining else -1

    private fun checkAvailable() {
        if (buffer.remaining == 0) {
            buffer.clear()
            input.read(buffer)
            buffer.flip()
        }
    }

    override fun read(dest: ByteBuffer): Int {
        checkAvailable()
        return buffer.read(dest)
    }

    override fun close() {
        buffer.close()
        input.close()
    }

    override fun read(): Char? {
        checkAvailable()
        if (buffer.remaining <= 0)
            return null
        return buffer.get().toChar()
    }

    override fun read(data: CharArray, offset: Int, length: Int): Int {
        checkAvailable()
        val len = minOf(minOf(data.size - offset, length), buffer.remaining)
        for (i in offset until offset + len) {
            data[i] = buffer.get().toChar()
        }
        return len
    }

    fun readUntil(char: Char): String? {
        val out = StringBuilder()
        var exist = false
        LOOP@ while (true) {
            checkAvailable()
            if (buffer.remaining <= 0) {
                break
            }
            for (i in buffer.position until buffer.limit) {
                buffer.position++
                if (buffer[i] == char.toByte()) {
                    exist = true
                    break@LOOP
                } else {
                    out.append(buffer[i].toChar())
                }

            }
            exist = true
        }
        if (!exist) {
            return null
        }
        return out.toString()
    }

    override fun readln(): String? = readUntil(10.toChar())?.removeSuffix("\r")
}

fun Input.bufferedAsciiReader(bufferSize: Int = DEFAULT_BUFFER_SIZE) = BufferedAsciiInputReader(
    input = this,
    bufferSize = bufferSize
)