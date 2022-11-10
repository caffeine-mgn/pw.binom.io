package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.pool.ObjectPool

abstract class AbstractAsyncBufferedAsciiWriter(
    val closeParent: Boolean
) : AsyncWriter, AsyncOutput {
    protected abstract val output: AsyncOutput
    protected abstract val buffer: ByteBuffer
    private var closed = false
    private fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }

    private suspend fun checkFlush() {
        if (buffer.remaining == 0) {
            flush()
        }
    }

    override suspend fun write(data: ByteBuffer): Int {
        checkClosed()
        var r = 0
        while (data.remaining > 0) {
            checkFlush()
            r += buffer.write(data)
        }
        return r
    }

    override suspend fun append(value: Char): AsyncAppendable {
        checkClosed()
        checkFlush()
        buffer.put(value.code.toByte())
        return this
    }

    override suspend fun append(value: CharSequence?): AsyncAppendable {
        value ?: return this
        append(value, 0, value.length)
        return this
    }

    override suspend fun append(value: CharSequence?, startIndex: Int, endIndex: Int): AsyncAppendable {
        checkClosed()
        value ?: return this
        if (value.isEmpty()) {
            return this
        }
        if (endIndex == startIndex) {
            return append(value[startIndex])
        }
        val data = ByteArray(endIndex - startIndex) {
            value[it].code.toByte()
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

    override suspend fun flush() {
        checkClosed()
        if (buffer.position > 0) {
            buffer.flip()
            output.writeFully(buffer)
//            while (buffer.remaining > 0) {
//                val v = output.write(buffer)
//                if (v <= 0) {
//                    throw IOException("Can't write data to output, v=$v")
//                }
//            }
            buffer.clear()
            output.flush()
        }
    }

    override suspend fun asyncClose() {
        checkClosed()
        flush()
        closed = true
        if (closeParent) {
            output.asyncClose()
        }
    }
}

class AsyncBufferedAsciiWriter private constructor(
    override val output: AsyncOutput,
    private val pool: ObjectPool<ByteBuffer>?,
    override val buffer: ByteBuffer,
    private var closeBuffer: Boolean,
    closeParent: Boolean = true
) :
    AbstractAsyncBufferedAsciiWriter(closeParent = closeParent) {
    override fun toString(): String = "AsyncBufferedAsciiWriter(closeBuffer=$closeBuffer, output=$output)"

    constructor(output: AsyncOutput, pool: ObjectPool<ByteBuffer>, closeParent: Boolean = true) : this(
        output = output,
        pool = pool,
        buffer = pool.borrow().clean(),
        closeBuffer = false,
        closeParent = closeParent,
    )

    constructor(output: AsyncOutput, bufferSize: Int = DEFAULT_BUFFER_SIZE, closeParent: Boolean = true) : this(
        output = output,
        pool = null,
        buffer = ByteBuffer.alloc(bufferSize).clean(),
        closeBuffer = true,
        closeParent = closeParent,
    )

    fun reset() {
        buffer.clear()
    }

    override suspend fun asyncClose() {
        super.asyncClose()
        if (closeBuffer) {
            buffer.close()
        } else {
            pool?.recycle(buffer)
        }
    }
}

fun AsyncOutput.bufferedAsciiWriter(pool: ObjectPool<ByteBuffer>, closeParent: Boolean = true) =
    AsyncBufferedAsciiWriter(
        output = this,
        pool = pool,
        closeParent = closeParent
    )

fun AsyncOutput.bufferedAsciiWriter(bufferSize: Int = DEFAULT_BUFFER_SIZE, closeParent: Boolean = true) =
    AsyncBufferedAsciiWriter(
        output = this,
        bufferSize = bufferSize,
        closeParent = closeParent
    )
