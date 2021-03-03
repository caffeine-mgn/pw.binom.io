package pw.binom.io

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE

abstract class AbstractAsyncBufferedAsciiWriter(
    val closeParent: Boolean
) : AsyncWriter, AsyncOutput {
    protected abstract val output: AsyncOutput

    protected abstract val buffer: ByteBuffer

    private suspend fun checkFlush() {
        if (buffer.remaining == 0) {
            flush()
        }
    }

    override suspend fun write(data: ByteBuffer): Int {
        var r = 0
        while (data.remaining > 0) {
            checkFlush()
            r += buffer.write(data)
        }
        return r
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
        (start..end).forEach {
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
            output.flush()
        }
    }

    override suspend fun asyncClose() {
        flush()
        if (closeParent) {
            output.asyncClose()
        }
    }
}

class AsyncBufferedAsciiWriter(
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    override val output: AsyncOutput,
    closeParent: Boolean = true
) :
    AbstractAsyncBufferedAsciiWriter(closeParent = closeParent) {
    init {
        require(bufferSize > 4)
    }

    fun reset() {
        buffer.clear()
    }

    override suspend fun asyncClose() {
        super.asyncClose()
        buffer.close()
    }

    override val buffer = ByteBuffer.alloc(bufferSize)
}

fun AsyncOutput.bufferedAsciiWriter(bufferSize: Int = DEFAULT_BUFFER_SIZE, closeParent: Boolean = true) =
    AsyncBufferedAsciiWriter(
        output = this,
        bufferSize = bufferSize,
        closeParent = closeParent
    )