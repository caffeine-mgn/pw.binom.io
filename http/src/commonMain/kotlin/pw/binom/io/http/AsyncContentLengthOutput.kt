package pw.binom.io.http

import pw.binom.atomic.AtomicBoolean
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.ClosedException

open class AsyncContentLengthOutput(
    val stream: AsyncOutput,
    val contentLength: ULong,
    val closeStream: Boolean = false
) : AsyncOutput {

    override suspend fun write(data: ByteBuffer): Int {
        ensureOpen()
        if (wrote >= contentLength) {
            return 0
//            throw IllegalStateException("All Content already send. ContentLength: [$contentLength], data.remaining: [${data.remaining}]")
        }
        var lim = data.limit
        if (wrote + data.remaining.toULong() > contentLength) {
            val l = data.position + (contentLength - wrote).toInt()
            if (l == 0) {
                return 0
            }
            data.limit = l
//            throw IllegalStateException("Can't send more than Content Length. ContentLength: [$contentLength], data.remaining: [${data.remaining}]")
        }
        try {
            val r = stream.write(data)
            wrote += r.toULong()
            return r
        } finally {
            data.limit = lim
        }
    }

    override suspend fun flush() {
        stream.flush()
    }

    override suspend fun asyncClose() {
        if (!closed.compareAndSet(false, true)) {
            return
        }
        flush()
        if (closeStream) {
            stream.asyncClose()
        }
    }

    var wrote = 0uL
        private set

    val isFull
        get() = wrote == contentLength

    private var closed = AtomicBoolean(false)

    protected fun ensureOpen() {
        if (closed.getValue()) {
            throw ClosedException()
        }
    }
}
