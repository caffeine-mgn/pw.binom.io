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

    private fun checkFlush() {
        if (charBuffer.remaining > 0) {
            return
        }
        flush()
    }

    override fun append(c: Char): Appendable {
        checkFlush()
        charBuffer.put(c)
        return this
    }

    override fun append(csq: CharSequence?): Appendable {
        csq?.forEach {
            append(it)
        }
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
        if (charBuffer.remaining == charBuffer.capacity) {
            return
        }
        val buffer = pool.borrow()
        try {
            charBuffer.flip()
            while (true) {
                buffer.clear()
                val r = encoder.encode(charBuffer, buffer)
                if (buffer.position > 0) {
                    buffer.flip()
                    output.write(buffer)
                    buffer.clear()
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
        flush()
        encoder.close()
    }
}

fun Output.bufferedAppendable(
    pool: ByteBufferPool,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = DEFAULT_BUFFER_SIZE / 2,
) = BufferedOutputAppendable(
    charset = charset,
    output = this,
    charBufferSize = charBufferSize,
    pool = pool
)