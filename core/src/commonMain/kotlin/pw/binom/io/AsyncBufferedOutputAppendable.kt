package pw.binom.io

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.charset.CharsetTransformResult
import pw.binom.charset.Charsets
import pw.binom.pool.ObjectPool

class AsyncBufferedOutputAppendable(
    charset: Charset,
    val output: AsyncOutput,
    private val pool: ObjectPool<ByteBuffer>,
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
        csq ?: return this
        return append(csq, 0, csq.lastIndex)
    }

    override suspend fun append(csq: CharSequence?, start: Int, end: Int): AsyncAppendable {
        csq ?: return this
        if (csq is String) {
            val array = csq.toCharArray()
            var pos = 0
            checkFlush()
            while (pos < end) {
                val wrote = charBuffer.write(array, pos, array.size - pos)
                if (wrote <= 0) {
                    throw IOException("Can't append data to")
                }
                pos += wrote
                checkFlush()
            }
        } else {
            (start..end).forEach {
                append(csq[it])
            }
        }
        return this
    }

    override suspend fun flush() {
        println("Flush----")
        if (charBuffer.remaining == charBuffer.capacity) {
            println("---#1")
            return
        }
        val buffer = pool.borrow()
        try {
            charBuffer.flip()
            while (true) {
                buffer.clear()
                println("Enciding ${charBuffer.remaining}...")
                val r = encoder.encode(charBuffer, buffer)
                println("Encode result $r")
                buffer.flip()
                println("Byte for send: ${buffer.remaining}")
                if (buffer.remaining != buffer.capacity) {
                    println("---#2")
                    output.write(buffer)
                    buffer.clear()
                }
                if (r == CharsetTransformResult.SUCCESS || charBuffer.remaining == 0) {
                    break
                }
            }
            println("---#3")
            charBuffer.clear()
        } finally {
            pool.recycle(buffer)
        }
    }

    override suspend fun asyncClose() {
        encoder.close()
    }
}

fun AsyncOutput.bufferedWriter(
    pool: ObjectPool<ByteBuffer>,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = 512
) = AsyncBufferedOutputAppendable(
    charset = charset,
    output = this,
    pool = pool,
    charBufferSize = charBufferSize
)