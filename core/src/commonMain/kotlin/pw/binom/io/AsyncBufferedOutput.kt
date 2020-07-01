package pw.binom.io

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE

class AsyncBufferedOutput(override val stream: AsyncOutput, bufferSize: Int = DEFAULT_BUFFER_SIZE) : AbstractAsyncBufferedOutput() {
    override val buffer = ByteBuffer.alloc(bufferSize)

    override suspend fun close() {
        try {
            super.close()
        } finally {
            stream.close()
            buffer.close()
        }
    }
}

abstract class AbstractAsyncBufferedOutput : AsyncOutput {
    protected abstract val stream: AsyncOutput
    protected abstract val buffer: ByteBuffer

    val bufferSize
        get() = buffer.capacity

    override suspend fun write(data: ByteBuffer): Int {
        var l = 0
        while (data.remaining > 0) {
            if (buffer.remaining <= 0)
                flush()
            l += buffer.write(data)
        }
        return l
    }

    override suspend fun flush() {
        buffer.flip()
        while (buffer.remaining > 0) {
            stream.write(buffer)
        }
        stream.flush()
        buffer.clear()
    }

    override suspend fun close() {
        flush()
    }
}

fun AsyncOutput.bufferedOutput(bufferSize: Int = DEFAULT_BUFFER_SIZE): AsyncBufferedOutput {
    if (this is AsyncBufferedOutput && this.bufferSize == bufferSize)
        return this
    return AsyncBufferedOutput(this, bufferSize)
}