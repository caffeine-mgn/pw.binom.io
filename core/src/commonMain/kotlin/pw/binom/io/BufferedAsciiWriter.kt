package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.Output

abstract class AbstractBufferedAsciiWriter : Writer, Output {
    protected abstract val output: Output

    protected abstract val buffer: ByteBuffer

    private fun checkFlush() {
        if (buffer.remaining == 0) {
            flush()
        }
    }

    override fun write(data: ByteBuffer): Int {
        var r = 0
        while (data.remaining > 0) {
            checkFlush()
            r += buffer.write(data)
        }
        return r
    }

    override fun append(c: Char): Appendable {
        checkFlush()
        buffer.put(c.toByte())
        return this
    }

    override fun append(csq: CharSequence?): Appendable {
        append(csq, 0, csq?.lastIndex ?: 0)
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): Appendable {
        csq ?: return this
        (start..end).forEach {
            append(csq[it])
        }
        return this
    }

    override fun flush() {
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
        flush()
        output.close()
    }
}

class BufferedAsciiWriter(bufferSize: Int = DEFAULT_BUFFER_SIZE, override val output: Output) :
    AbstractBufferedAsciiWriter() {
    init {
        require(bufferSize > 4)
    }

    fun reset() {
        buffer.clear()
    }

    override fun close() {
        buffer.close()
    }

    override val buffer = ByteBuffer.alloc(bufferSize)
}

fun Output.bufferedAsciiWriter(bufferSize: Int = DEFAULT_BUFFER_SIZE) = BufferedAsciiWriter(
    output = this,
    bufferSize = bufferSize
)