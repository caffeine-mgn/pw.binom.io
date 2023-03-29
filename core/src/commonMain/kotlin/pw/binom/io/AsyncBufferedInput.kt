package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE

class AsyncBufferedInput(
    override val stream: AsyncInput,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    private val closeStream: Boolean,
) : AbstractAsyncBufferedInput() {
    override val buffer: ByteBuffer = ByteBuffer(bufferSize).empty()

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

    private var internalReadBytes = 0L
    val readBytes
        get() = internalReadBytes

    override val available: Int
        get() = if (buffer.remaining == 0) -1 else buffer.remaining

    protected var closed = false

    protected open suspend fun fill() {
        stream.read(buffer)
        buffer.flip()
    }

    suspend fun readByte(): Byte {
        if (buffer.remaining <= 0) {
            fill()
        }
        if (buffer.remaining <= 0) {
            throw EOFException()
        }
        val byte = buffer.getByte()
        internalReadBytes++
        return byte
    }

    suspend fun readByteArray(dataSize: Int): ByteArray {
        require(dataSize >= 0) { "dataSize should be equals or greater than 0" }
        if (dataSize == 0) {
            return byteArrayOf()
        }
        val data = ByteArray(dataSize)
        readByteArray(data)
        return data
    }

    suspend fun readByteArray(data: ByteArray) {
        if (data.isEmpty()) {
            return
        }
        for (i in data.indices) {
            data[i] = readByte()
        }
    }

    override suspend fun read(dest: ByteBuffer): Int {
        if (buffer.remaining == 0) {
            fill()
        }
        val read = dest.write(buffer)
        internalReadBytes += read
        return read
    }

    override suspend fun asyncClose() {
        checkClosed()
        closed = true
    }

    protected fun checkClosed() {
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
