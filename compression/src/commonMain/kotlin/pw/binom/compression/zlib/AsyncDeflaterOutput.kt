package pw.binom.compression.zlib

import pw.binom.ByteBufferPool
import pw.binom.io.*

open class AsyncDeflaterOutput protected constructor(
    val stream: AsyncOutput,
    level: Int = 6,
    val buffer: ByteBuffer,
    wrap: Boolean = false,
    syncFlush: Boolean = true,
    private val pool: ByteBufferPool?,
    private var closeBuffer: Boolean,
    val closeStream: Boolean = false,
) : AsyncOutput {

    constructor(
        stream: AsyncOutput,
        level: Int = 6,
        bufferSize: Int = 512,
        wrap: Boolean = false,
        syncFlush: Boolean = true,
        closeStream: Boolean = false,
    ) : this(
        stream = stream,
        level = level,
        buffer = ByteBuffer(bufferSize),
        wrap = wrap,
        syncFlush = syncFlush,
        pool = null,
        closeBuffer = true,
        closeStream = closeStream,
    )

    constructor(
        stream: AsyncOutput,
        level: Int = 6,
        bufferPool: ByteBufferPool,
        wrap: Boolean = false,
        syncFlush: Boolean = true,
        closeStream: Boolean = false,
    ) : this(
        stream = stream,
        level = level,
        buffer = bufferPool.borrow(),
        wrap = wrap,
        syncFlush = syncFlush,
        pool = bufferPool,
        closeBuffer = false,
        closeStream = closeStream,
    )

    private val deflater = Deflater(level, wrap, syncFlush)

    //    private val buffer = ByteBuffer(bufferSize)
    protected val buf
        get() = buffer

    val totalIn: Long
        get() = deflater.totalIn
    val totalOut: Long
        get() = deflater.totalOut

    protected val def
        get() = deflater
    private var closed = false

    protected var usesDefaultDeflater = true
    private var busy = false
    private fun checkBusy() {
        if (busy) {
            throw IllegalStateException("Output is busy")
        }
    }

    override suspend fun write(data: ByteBuffer): Int {
        checkBusy()
        try {
            busy = true
            val vv = data.remaining
            while (true) {
                buffer.clear()
                val l = deflater.deflate(data, buffer)

                buffer.flip()
                while (buffer.remaining > 0) {
                    stream.write(buffer)
                }

                if (l <= 0) {
                    break
                }
            }
            return vv
        } finally {
            busy = false
        }
    }

    private fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }

    override suspend fun flush() {
        checkClosed()
        checkBusy()
        try {
            busy = true
            while (true) {
                buffer.clear()
                val r = deflater.flush(buffer)
                val writed = buffer.position
                buffer.flip()
                if (writed > 0) {
                    stream.write(buffer)
                }
                if (!r) {
                    break
                }
            }
            stream.flush()
        } finally {
            busy = false
        }
    }

    protected open suspend fun finish() {
        checkClosed()
        deflater.finish()
        flush()
        if (usesDefaultDeflater) {
            deflater.end()
        }
    }

    override suspend fun asyncClose() {
        if (closed) {
            return
        }
        checkBusy()
        finish()
        closed = true
        try {
            busy = true
            if (closeBuffer) {
                buffer.close()
            } else {
                pool?.recycle(buffer as PooledByteBuffer)
            }
            Closeable.close(deflater)
            if (closeStream) {
                stream.asyncClose()
            }
        } finally {
            busy = false
        }
    }
}
