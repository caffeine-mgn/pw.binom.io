package pw.binom.io

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.empty

class AsyncBufferedInput(
        override val stream: AsyncInput,
        bufferSize: Int = DEFAULT_BUFFER_SIZE,
        private val closeStream: Boolean
) : AbstractAsyncBufferedInput() {
    override val buffer: ByteBuffer = ByteBuffer.alloc(bufferSize).empty()

    override suspend fun close() {
        try {
            super.close()
        } finally {
            if (closeStream) {
                stream.close()
            }
            buffer.close()
        }
    }
}

abstract class AbstractAsyncBufferedInput : AsyncInput {
    protected abstract val buffer: ByteBuffer
    protected abstract val stream: AsyncInput

    val available
        get() = if (buffer.remaining == 0) -1 else buffer.remaining

    protected var closed = false

//    override suspend fun skip(length: Long): Long {
//        val buf = ByteBuffer.alloc(512)
//        var l = length
//        while (l > 0) {
//            buf.reset(0, minOf(buf.capacity, l.toInt()))
//            read(buf)
//        }
//        return l
//    }

//    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        if (available == -1) {
//            readed.value = 0
//            wrote.value = stream.read(buffer, 0, buffer.size)
//            if (wrote.value <= 0)
//                return wrote.value
//        }
//
//        val l = minOf(wrote.value - readed.value, length)
//
//        buffer.copyInto(data, destinationOffset = offset, startIndex = readed.value, endIndex = readed.value + l)
//        readed.addAndGet(l)
//        return l
//    }

    protected open suspend fun fill() {
        buffer.clear()
        stream.read(buffer)
        buffer.flip()
    }

    override suspend fun read(dest: ByteBuffer): Int {
        if (buffer.remaining == 0) {
            fill()
        }
        return dest.write(buffer)
    }

    override suspend fun close() {
        checkClosed()
        closed = true
    }

    protected inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }
}

fun AsyncInput.bufferedInput(bufferSize: Int = DEFAULT_BUFFER_SIZE, closeStream: Boolean = true) =
        AsyncBufferedInput(stream = this, bufferSize = bufferSize, closeStream = closeStream)