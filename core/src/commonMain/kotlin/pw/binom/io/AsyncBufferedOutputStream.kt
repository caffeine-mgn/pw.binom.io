package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE

class AsyncBufferedOutputStream(val stream: AsyncOutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE) : AsyncOutputStream {
    private val buffer = ByteArray(bufferSize)
    private var cursor = 0
    val bufferSize
        get() = buffer.size

    override suspend fun write(data: Byte): Boolean {
        if (cursor == buffer.size)
            flush()
        buffer[cursor++] = data
        return true
    }

    override suspend fun write(data: ByteArray, offset: Int, length: Int): Int {
        var off = offset
        var len = length
//        var cursor = cursor
        while (len > 0) {
            if (cursor == buffer.size)
                flush()
            val bb = minOf(buffer.size - cursor, len)

            data.copyInto(buffer, cursor, off, off + bb)
            len -= bb
            off += bb
            cursor += bb
        }
//        this.cursor = cursor
        return length - len
    }

    override suspend fun flush() {
        while (cursor > 0) {
            cursor -= stream.write(buffer, 0, cursor)
            stream.flush()
        }
    }

    override suspend fun close() {
        flush()
        stream.close()
    }
}

fun AsyncOutputStream.buffered(bufferSize: Int = DEFAULT_BUFFER_SIZE): AsyncBufferedOutputStream {
    if (this is AsyncBufferedOutputStream && this.bufferSize == bufferSize)
        return this
    return AsyncBufferedOutputStream(this, bufferSize)
}