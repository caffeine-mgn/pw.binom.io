package pw.binom.io.http

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.*

open class AsyncChunkedOutputStream(
        val stream: AsyncOutputStream,
        private val autoFlushBuffer: Int = DEFAULT_BUFFER_SIZE
) : AsyncOutputStream {
    override suspend fun write(data: Byte): Boolean {
        checkClosed()
        val r = buffer.write(data)
        if (buffer.size >= autoFlushBuffer)
            flush()
        return r
    }

    private var closed = false
    var buffer = ByteArrayOutputStream(autoFlushBuffer)
    override suspend fun write(data: ByteArray, offset: Int, length: Int): Int {
        checkClosed()
        val r = buffer.write(data, offset, length)
        if (buffer.size >= autoFlushBuffer)
            flush()
        return r
    }

    override suspend fun flush() {
        checkClosed()
        if (buffer.size == 0)
            return
        stream.writeln(buffer.size.toString(16))
        stream.write(buffer.toByteArray())
        stream.writeln("")
        stream.flush()
        buffer = ByteArrayOutputStream(autoFlushBuffer)
    }

    override suspend fun close() {
        checkClosed()
        flush()
        stream.write("0\r\n")
        stream.write("\r\n")
        stream.flush()
        closed = true
    }

    protected fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }
}