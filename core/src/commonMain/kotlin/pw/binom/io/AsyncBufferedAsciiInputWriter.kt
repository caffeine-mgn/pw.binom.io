package pw.binom.io

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE

class AsyncBufferedAsciiInputWriter(
    val output: AsyncOutput,
    val bufferSize: Int = DEFAULT_BUFFER_SIZE,
) : AsyncWriter, AsyncOutput {

    init {
        require(bufferSize > 4)
    }

    private val buffer = ByteBuffer.alloc(bufferSize)

    fun reset() {
        buffer.clear()
    }

    private suspend fun checkFlush() {
        if (buffer.remaining == 0) {
            flush()
        }
    }

    override suspend fun write(data: ByteBuffer): Int {
        checkFlush()
        return buffer.write(data)
    }

    override suspend fun append(c: Char): AsyncAppendable {
        checkFlush()
        buffer.put(c.toByte())
        return this
    }

    override suspend fun append(csq: CharSequence?): AsyncAppendable {
        append(csq, 0, csq?.lastIndex ?: 0)
        return this
    }

    override suspend fun append(csq: CharSequence?, start: Int, end: Int): AsyncAppendable {
        csq ?: return this
        var start = start
        (start..end).forEach {
            checkFlush()
            append(csq[it])
        }
        return this
    }

    override suspend fun flush() {
        if (buffer.remaining != buffer.capacity) {
            buffer.flip()
            while (buffer.remaining > 0) {
                output.write(buffer)
            }
            buffer.clear()
        }
    }

    override suspend fun asyncClose() {
        flush()
        output.asyncClose()
    }
}

fun AsyncOutput.bufferedAsciiInputWriter(bufferSize: Int = DEFAULT_BUFFER_SIZE) = AsyncBufferedAsciiInputWriter(
    output = this,
    bufferSize = bufferSize
)