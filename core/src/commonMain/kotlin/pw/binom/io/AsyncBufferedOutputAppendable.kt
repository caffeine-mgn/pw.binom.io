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
        if (csq.length > 1 && csq is String) {
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
        if (charBuffer.remaining == charBuffer.capacity) {
            return
        }
        charBuffer.flip()
        while (true) {
            buffer.clear()
            val r = encoder.encode(charBuffer, buffer)
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
        flush()
        encoder.close()
        if (closeBuffer) {
            buffer.close()
        } else {
            pool?.recycle(buffer)
        }
        if (closeParent) {
            output.asyncClose()
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
    bufferSize: Int= DEFAULT_BUFFER_SIZE,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = bufferSize/2,
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
    charBufferSize: Int = buffer.capacity/2,
    closeParent: Boolean = true
) = AsyncBufferedOutputAppendable(
    charset = charset,
    output = this,
    buffer = buffer,
    charBufferSize = charBufferSize,
    closeParent = closeParent
)