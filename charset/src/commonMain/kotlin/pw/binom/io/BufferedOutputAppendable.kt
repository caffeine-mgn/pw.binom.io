package pw.binom.io

import pw.binom.ByteBufferPool
import pw.binom.CharBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.charset.Charset
import pw.binom.charset.CharsetTransformResult
import pw.binom.charset.Charsets

class BufferedOutputAppendable private constructor(
    charset: Charset,
    val output: Output,
    private val pool: ByteBufferPool?,
    charBufferSize: Int,
    val closeParent: Boolean,
    private val buffer: ByteBuffer,
    private var closeBuffer: Boolean,
) : Appendable, Flushable, Closeable {

    constructor(
        charset: Charset = Charsets.UTF8,
        output: Output,
        pool: ByteBufferPool,
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
        output: Output,
        bufferSize: Int = DEFAULT_BUFFER_SIZE,
        charBufferSize: Int = bufferSize / 2,
        closeParent: Boolean = true,
    ) : this(
        charset = charset,
        output = output,
        pool = null,
        charBufferSize = charBufferSize,
        closeParent = closeParent,
        buffer = ByteBuffer(bufferSize),
        closeBuffer = true,
    )

    constructor(
        charset: Charset = Charsets.UTF8,
        output: Output,
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

    override fun append(value: Char): Appendable {
        checkClosed()
        checkFlush()
        charBuffer.put(value)
        return this
    }

    override fun append(value: CharSequence?): Appendable {
        value ?: return this
        return append(value, 0, value.length)
    }

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable {
        checkClosed()
        value ?: return this
        if (value.isEmpty()) {
            return this
        }
        if (startIndex == endIndex) {
            checkFlush()
            charBuffer.put(value[startIndex])
            return this
        }
        val array = if (value is String) {
            value.toCharArray(startIndex, endIndex)
        } else {
            CharArray(endIndex - startIndex) {
                value[it + startIndex]
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
        charBuffer.flip()
        while (true) {
            buffer.clear()
            val sourcePosition = buffer.position
            val r = encoder.encode(charBuffer, buffer)
            if (buffer.position > sourcePosition) {
                buffer.flip()
                output.writeFully(buffer)
            }
            if (r == CharsetTransformResult.SUCCESS || charBuffer.remaining == 0) {
                break
            }
        }
        charBuffer.clear()
    }

    override fun close() {
        checkClosed()
        flush()
        try {
            encoder.close()
            if (closeBuffer) {
                buffer.close()
            } else {
                pool?.recycle(buffer as PooledByteBuffer)
            }
            if (closeParent) {
                output.close()
            }
        } finally {
            closed = true
        }
    }
}

fun Output.bufferedWriter(
    pool: ByteBufferPool,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = 512,
    closeParent: Boolean = true,
) = BufferedOutputAppendable(
    charset = charset,
    output = this,
    pool = pool,
    charBufferSize = charBufferSize,
    closeParent = closeParent,
)

fun Output.bufferedWriter(
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = bufferSize / 2,
    closeParent: Boolean = true,
) = BufferedOutputAppendable(
    charset = charset,
    output = this,
    bufferSize = bufferSize,
    charBufferSize = charBufferSize,
    closeParent = closeParent,
)

fun Output.bufferedWriter(
    buffer: ByteBuffer,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = buffer.capacity / 2,
    closeParent: Boolean = true,
) = BufferedOutputAppendable(
    charset = charset,
    output = this,
    buffer = buffer,
    charBufferSize = charBufferSize,
    closeParent = closeParent,
)
