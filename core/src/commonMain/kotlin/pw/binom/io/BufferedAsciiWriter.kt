package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.Output

abstract class AbstractBufferedAsciiWriter : Writer, Output {
    protected abstract val output: Output

    protected abstract val buffer: ByteBuffer
    private var closed = false

    private fun checkFlush() {
        if (buffer.remaining == 0) {
            flush()
        }
    }

    private fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }

    override fun write(data: ByteBuffer): Int {
        checkClosed()
        var r = 0
        while (data.remaining > 0) {
            checkFlush()
            r += buffer.write(data)
        }
        return r
    }

    override fun append(c: Char): Appendable {
        checkClosed()
        checkFlush()
        buffer.put(c.toByte())
        return this
    }

    override fun append(csq: CharSequence?): Appendable {
        csq ?: return this
        append(csq, 0, csq.length)
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): Appendable {
        checkClosed()
        csq ?: return this
        if (csq.isEmpty()) {
            return this
        }
        if (end == start) {
            return append(csq[start])
        }
        val data = ByteArray(end - start) {
            csq[it].toByte()
        }
        var pos = 0
        while (pos < data.size) {
            checkFlush()
            val wrote = buffer.write(data, offset = pos)
            if (wrote <= 0) {
                throw IOException("Can't append data to")
            }
            pos += wrote
        }
        return this
    }

    override fun flush() {
        checkClosed()
        if (buffer.remaining != buffer.capacity) {
            buffer.flip()
            while (buffer.remaining > 0) {
                output.write(buffer)
            }
            buffer.clear()
            output.flush()
        }
    }

    override fun close() {
        checkClosed()
        flush()
        closed = true
    }
}

class BufferedAsciiWriter(
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    val closeParent: Boolean,
    override val output: Output
) :
    AbstractBufferedAsciiWriter() {
    init {
        require(bufferSize > 4)
    }

    fun reset() {
        buffer.clear()
    }

    override fun close() {
        try {
            super.close()
        } finally {
            buffer.close()
            if (closeParent) {
                output.close()
            }
        }
    }

    override val buffer = ByteBuffer.alloc(bufferSize)
}

fun Output.bufferedAsciiWriter(bufferSize: Int = DEFAULT_BUFFER_SIZE, closeParent: Boolean = true) =
    BufferedAsciiWriter(
        output = this,
        bufferSize = bufferSize,
        closeParent = closeParent,
    )