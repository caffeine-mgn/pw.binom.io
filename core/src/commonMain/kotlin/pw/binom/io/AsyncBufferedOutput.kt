package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE

class AsyncBufferedOutput(
    override val stream: AsyncOutput,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    private val closeStream: Boolean
) : AbstractAsyncBufferedOutput() {
    override val buffer = ByteBuffer.alloc(bufferSize)

    override suspend fun asyncClose() {
        try {
            super.asyncClose()
        } finally {
            if (closeStream) {
                stream.asyncClose()
            }
            buffer.close()
        }
    }
}

abstract class AbstractAsyncBufferedOutput : AsyncOutput {
    protected abstract val stream: AsyncOutput
    protected abstract val buffer: ByteBuffer
    private var closed = false
    val bufferSize
        get() = buffer.capacity

    private fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }

    override suspend fun write(data: ByteBuffer): Int {
        checkClosed()
        var l = 0
        while (data.remaining123 > 0) {
            if (buffer.remaining123 <= 0)
                flush()
            l += buffer.write(data)
        }
        return l
    }

    override suspend fun flush() {
        checkClosed()
        buffer.flip()
        while (buffer.remaining123 > 0) {
            stream.write(buffer)
        }
        stream.flush()
        buffer.clear()
    }

    override suspend fun asyncClose() {
        checkClosed()
        flush()
        closed = true
    }
}

fun AsyncOutput.bufferedOutput(
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    closeStream: Boolean = true
): AsyncBufferedOutput {
    if (this is AsyncBufferedOutput && this.bufferSize == bufferSize)
        return this
    return AsyncBufferedOutput(
        stream = this,
        bufferSize = bufferSize,
        closeStream = closeStream
    )
}
