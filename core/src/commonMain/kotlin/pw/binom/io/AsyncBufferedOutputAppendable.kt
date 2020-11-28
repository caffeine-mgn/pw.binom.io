package pw.binom.io

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.charset.CharsetTransformResult

class AsyncBufferedOutputAppendable(
    charset: Charset,
    val output: AsyncOutput,
    private val pool: ByteBufferPool,
    charBufferSize: Int = DEFAULT_BUFFER_SIZE / 2
) : AsyncAppendable, AsyncFlushable, AsyncCloseable {

    val charBuffer = CharBuffer.alloc(charBufferSize)
    val encoder = charset.newEncoder()

    private suspend fun checkFlush() {
        if (charBuffer.remaining > 0) {
            return
        }
        flush()
    }

    override suspend fun append(c: Char): AsyncAppendable {
        checkFlush()
        charBuffer.put(c)
        return this
    }

    override suspend fun append(csq: CharSequence?): AsyncAppendable {
        csq?.forEach {
            append(it)
        }
        return this
    }

    override suspend fun append(csq: CharSequence?, start: Int, end: Int): AsyncAppendable {
        csq ?: return this
        (start..end).forEach {
            append(csq[it])
        }
        return this
    }

    override suspend fun flush() {
        if (charBuffer.remaining == charBuffer.capacity) {
            return
        }
        val buffer = pool.borrow()
        try {
            charBuffer.flip()
            while (true) {
                buffer.clear()
                val r = encoder.encode(charBuffer, buffer)
                if (r == CharsetTransformResult.SUCCESS || charBuffer.remaining == 0) {
                    break
                }
                buffer.flip()
                if (buffer.remaining != buffer.capacity) {
                    output.write(buffer)
                    buffer.clear()
                }
            }
            charBuffer.clear()
        } finally {
            pool.recycle(buffer)
        }
    }

    override suspend fun asyncClose() {
        encoder.close()
    }

}