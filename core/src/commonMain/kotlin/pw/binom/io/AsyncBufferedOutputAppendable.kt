package pw.binom.io

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.CharBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.charset.Charset
import pw.binom.charset.CharsetTransformResult
import pw.binom.charset.Charsets
import pw.binom.pool.ObjectPool

class AsyncBufferedOutputAppendable private constructor(
    charset: Charset,
    val output: AsyncOutput,
    private val pool: ObjectPool<ByteBuffer>?,
    charBufferSize: Int,
    val closeParent: Boolean,
    private val buffer: ByteBuffer,
    private var closeBuffer: Boolean,
) : AsyncWriter, AsyncFlushable, AsyncCloseable {

    constructor(
        charset: Charset = Charsets.UTF8,
        output: AsyncOutput,
        pool: ObjectPool<ByteBuffer>,
        charBufferSize: Int = DEFAULT_BUFFER_SIZE / 2,
        closeParent: Boolean = true,
    ) : this(
        charset = charset,
        output = output,
        pool = pool,
        charBufferSize = charBufferSize,
        closeParent = closeParent,
        buffer = pool.borrow(),
        closeBuffer = false,
    )

    constructor(
        charset: Charset = Charsets.UTF8,
        output: AsyncOutput,
        bufferSize: Int = DEFAULT_BUFFER_SIZE,
        charBufferSize: Int = bufferSize / 2,
        closeParent: Boolean = true,
    ) : this(
        charset = charset,
        output = output,
        pool = null,
        charBufferSize = charBufferSize,
        closeParent = closeParent,
        buffer = ByteBuffer.alloc(bufferSize),
        closeBuffer = true,
    )

    constructor(
        charset: Charset = Charsets.UTF8,
        output: AsyncOutput,
        buffer: ByteBuffer,
        charBufferSize: Int = buffer.capacity / 2,
        closeParent: Boolean = true,
    ) : this(
        charset = charset,
        output = output,
        pool = null,
        charBufferSize = charBufferSize,
        closeParent = closeParent,
        buffer = buffer,
        closeBuffer = false,
    )

    private val charBuffer = CharBuffer.alloc(charBufferSize)
    private val encoder = charset.newEncoder()
    private var closed = false
    val isClosed
        get() = closed

    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("Writer already closed")
        }
    }

    private suspend fun checkFlush() {
        if (charBuffer.remaining > 0) {
            return
        }
        flush()
    }

    override suspend fun append(c: Char): AsyncAppendable {
        checkClosed()
        checkFlush()
        charBuffer.put(c)
        return this
    }

    override suspend fun append(csq: CharSequence?): AsyncAppendable {
        checkClosed()
        csq ?: return this
        return append(csq, 0, csq.length)
    }

    override suspend fun append(csq: CharSequence?, start: Int, end: Int): AsyncAppendable {
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

    override suspend fun flush() {
        checkClosed()
        if (charBuffer.remaining == charBuffer.capacity) {
            return
        }
        charBuffer.flip()
        while (true) {
            buffer.clear()
            val r = encoder.encode(charBuffer, buffer)
            if (r == CharsetTransformResult.MALFORMED) {
                throw RuntimeException("Malformed String")
            }
            buffer.flip()
            if (buffer.remaining != buffer.capacity) {
                output.write(buffer)
                buffer.clear()
            }
            if (r == CharsetTransformResult.SUCCESS || charBuffer.remaining == 0) {
                break
            }
        }
        charBuffer.clear()
    }

    override suspend fun asyncClose() {
        checkClosed()
        flush()
        try {
            encoder.close()
            if (closeBuffer) {
                buffer.close()
            } else {
                pool?.recycle(buffer)
            }
            if (closeParent) {
                output.asyncClose()
            }
        } finally {
            closed = true
        }
    }
}

fun AsyncOutput.bufferedWriter(
    pool: ObjectPool<ByteBuffer>,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = 512,
    closeParent: Boolean = true
) = AsyncBufferedOutputAppendable(
    charset = charset,
    output = this,
    pool = pool,
    charBufferSize = charBufferSize,
    closeParent = closeParent
)

fun AsyncOutput.bufferedWriter(
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = bufferSize / 2,
    closeParent: Boolean = true
) = AsyncBufferedOutputAppendable(
    charset = charset,
    output = this,
    bufferSize = bufferSize,
    charBufferSize = charBufferSize,
    closeParent = closeParent
)

fun AsyncOutput.bufferedWriter(
    buffer: ByteBuffer,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = buffer.capacity / 2,
    closeParent: Boolean = true
) = AsyncBufferedOutputAppendable(
    charset = charset,
    output = this,
    buffer = buffer,
    charBufferSize = charBufferSize,
    closeParent = closeParent
)