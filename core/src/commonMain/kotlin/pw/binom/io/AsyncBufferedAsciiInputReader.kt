package pw.binom.io

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.empty

class AsyncBufferedAsciiInputReader(
    val input: AsyncInput,
    val bufferSize: Int = DEFAULT_BUFFER_SIZE,
) : AsyncReader, AsyncInput {
    init {
        require(bufferSize > 4)
    }

    fun reset() {
        buffer.empty()
    }

    private val buffer = ByteBuffer.alloc(bufferSize).empty()

    override val available: Int
        get() = if (buffer.remaining > 0) buffer.remaining else -1

    private suspend fun checkAvailable() {
        if (buffer.remaining == 0) {
            buffer.clear()
            input.read(buffer)
            buffer.flip()
        }
    }

    override suspend fun read(dest: ByteBuffer): Int {
        checkAvailable()
        return buffer.read(dest)
    }

    override suspend fun asyncClose() {
        buffer.close()
        input.asyncClose()
    }

    override suspend fun readChar(): Char? {
        checkAvailable()
        if (buffer.remaining <= 0)
            return null
        return buffer.get().toChar()
    }

    override suspend fun read(data: CharArray, offset: Int, length: Int): Int {
        checkAvailable()
        val len = minOf(minOf(data.size - offset, length), buffer.remaining)
        for (i in offset until offset + len) {
            data[i] = buffer.get().toChar()
        }
        return len
    }

    suspend fun readUntil(char: Char): String? {
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

    override suspend fun readln(): String? = readUntil(10.toChar())?.removeSuffix("\r")
}

fun AsyncInput.bufferedAsciiInputReader(bufferSize: Int = DEFAULT_BUFFER_SIZE) = AsyncBufferedAsciiInputReader(
    input = this,
    bufferSize = bufferSize
)