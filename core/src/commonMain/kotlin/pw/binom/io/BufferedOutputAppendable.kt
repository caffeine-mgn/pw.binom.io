package pw.binom.io

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.charset.CharsetTransformResult
import pw.binom.charset.Charsets

class BufferedOutputAppendable(
    charset: Charset,
    val output: Output,
    private val pool: ByteBufferPool,
    charBufferSize: Int = DEFAULT_BUFFER_SIZE / 2
) : Appendable, Flushable, Closeable {

    val charBuffer = CharBuffer.alloc(charBufferSize)
    val encoder = charset.newEncoder()

    private var closed = false
    val isClosed
        get() = closed

    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("Writer already closed")
        }
    }

    private fun checkFlush() {
        if (charBuffer.remaining > 0) {
            return
        }
        flush()
    }

    override fun append(c: Char): Appendable {
        checkClosed()
        checkFlush()
        charBuffer.put(c)
        return this
    }

    override fun append(csq: CharSequence?): Appendable {
        csq ?: return this
        return append(csq, 0, csq.length)
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): Appendable {
        checkClosed()
        csq ?: return this
        if (csq.isEmpty()) {
            return this
        }
        if (start == end) {
            checkFlush()
            charBuffer.put(csq[start])
            return this
        }
        val array = if (csq is String) {
            csq.toCharArray(start, end)
        } else {
            CharArray(end - start) {
                csq[it + start]
            }
        }
        var pos = 0
        while (pos < array.size) {
            checkFlush()
            val wrote = charBuffer.write(array, pos)
            if (wrote <= 0) {
                throw IOException("Can't append data to")
            }
            pos += wrote
        }

        return this
    }

    override fun flush() {
        checkClosed()
        if (charBuffer.remaining == charBuffer.capacity) {
            return
        }
        val buffer = pool.borrow()
        try {
            charBuffer.flip()
            while (true) {
                buffer.clear()
                val oldr = charBuffer.remaining
                val r = encoder.encode(charBuffer, buffer)
                if (buffer.position > 0) {
                    buffer.flip()
                    output.write(buffer)
                    buffer.clear()
                }
                if (r == CharsetTransformResult.INPUT_OVER) {
                    println("charBuffer.remaining: ${charBuffer.remaining}  ${charBuffer[14]}")
                    TODO()
                }
                if (r == CharsetTransformResult.SUCCESS || charBuffer.remaining == 0) {
                    break
                }
            }
            charBuffer.clear()
        } finally {
            pool.recycle(buffer)
        }
    }

    override fun close() {
        checkClosed()
        flush()
        encoder.close()
    }
}

fun Output.bufferedWriter(
    pool: ByteBufferPool,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = DEFAULT_BUFFER_SIZE / 2,
) = BufferedOutputAppendable(
    charset = charset,
    output = this,
    charBufferSize = charBufferSize,
    pool = pool
)