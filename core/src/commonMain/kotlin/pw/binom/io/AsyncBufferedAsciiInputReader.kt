package pw.binom.io

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.empty

class AsyncBufferedAsciiInputReader(
    val stream: AsyncInput,
    val bufferSize: Int = DEFAULT_BUFFER_SIZE,
    val closeParent: Boolean = true,
) : AsyncReader, AsyncInput {
    init {
        require(bufferSize > 4)
    }

    private var eof = false

    fun reset() {
        buffer.empty()
    }

    private val buffer = ByteBuffer.alloc(bufferSize).empty()

    override val available: Int
        get() = if (buffer.remaining > 0) buffer.remaining else -1

    private suspend fun full() {
        if (eof) {
            return
        }
        if (buffer.remaining == 0) {
            buffer.clear()
            if (stream.read(buffer) == 0) {
                eof = true
            }
            buffer.flip()
        }
    }

    override suspend fun read(dest: ByteBuffer): Int {
        full()
        return buffer.read(dest)
    }

    override suspend fun asyncClose() {
        buffer.close()
        if (closeParent) {
            stream.asyncClose()
        }
    }

    override suspend fun readChar(): Char? {
        full()
        if (buffer.remaining <= 0)
            return null
        return buffer.get().toChar()
    }

    override suspend fun read(dest: CharArray, offset: Int, length: Int): Int {
        full()
        val len = minOf(minOf(dest.size - offset, length), buffer.remaining)
        for (i in offset until offset + len) {
            dest[i] = buffer.get().toChar()
        }
        return len
    }

    suspend fun read(dest: ByteArray, offset: Int = 0, length: Int = dest.size - offset): Int {
        full()
        val len = minOf(minOf(dest.size - offset, length), buffer.remaining)
        buffer.get(
            dest = dest,
            offset = offset,
            length = len,
        )
        return len
    }

    suspend fun readFully(dest: ByteArray, offset: Int = 0, length: Int = dest.size - offset): Int {
        var readed = 0
        while (true) {
            val r = read(dest, offset + readed, length - readed)
            readed += r
            if (readed == length) {
                return length
            }
        }
    }

    suspend fun readUntil(char: Char): String? {
        val out = StringBuilder()
        var exist = false
        LOOP@ while (true) {
            full()
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

fun AsyncInput.bufferedAsciiReader(bufferSize: Int = DEFAULT_BUFFER_SIZE, closeParent: Boolean = true) =
    AsyncBufferedAsciiInputReader(
        stream = this,
        bufferSize = bufferSize,
        closeParent = closeParent,
    )