package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE

class AsyncBufferedInput(
    override val stream: AsyncInput,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    private val closeStream: Boolean
) : AbstractAsyncBufferedInput() {
    override val buffer: ByteBuffer = ByteBuffer.alloc(bufferSize).empty()

    override suspend fun fill() {
        buffer.clear()
        try {
            super.fill()
        } catch (e: Throwable) {
            buffer.empty()
            throw e
        }
    }

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

abstract class AbstractAsyncBufferedInput : AsyncInput {
    protected abstract val buffer: ByteBuffer
    protected abstract val stream: AsyncInput

    override val available: Int
        get() = if (buffer.remaining == 0) -1 else buffer.remaining

    protected var closed = false

    protected open suspend fun fill() {
        stream.read(buffer)
        buffer.flip()
    }

    override suspend fun read(dest: ByteBuffer): Int {
        if (buffer.remaining == 0) {
            fill()
        }
        return dest.write(buffer)
    }

    override suspend fun asyncClose() {
        checkClosed()
        closed = true
    }

    protected inline fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }
}

fun AsyncInput.bufferedInput(bufferSize: Int = DEFAULT_BUFFER_SIZE, closeParent: Boolean = true): AsyncBufferedInput {
    if (this is AsyncBufferedInput) {
        return this
    }
    return AsyncBufferedInput(stream = this, bufferSize = bufferSize, closeStream = closeParent)
}
