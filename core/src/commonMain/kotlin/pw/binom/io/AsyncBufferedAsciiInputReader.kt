package pw.binom.io

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.empty
import pw.binom.pool.ObjectPool

class AsyncBufferedAsciiInputReader(
    val input: AsyncInput,
    private val pool: ObjectPool<ByteBuffer>,
) : AsyncReader, AsyncInput {

    private val buffer = pool.borrow().empty()

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
        pool.recycle(buffer)
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
}