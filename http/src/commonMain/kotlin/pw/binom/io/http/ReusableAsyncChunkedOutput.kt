package pw.binom.io.http

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.NullAsyncOutput
import pw.binom.io.AsyncOutput
import pw.binom.io.Closeable
import pw.binom.pool.DefaultPool

open class ReusableAsyncChunkedOutput(
    autoFlushBuffer: Int = DEFAULT_BUFFER_SIZE,
    var onRevert: ((ReusableAsyncChunkedOutput) -> Unit)? = null
) : AsyncChunkedOutput(
    stream = NullAsyncOutput,
    closeStream = false,
    autoFlushBuffer = autoFlushBuffer,
) {

    class Pool(capasity: Int, autoFlushBuffer: Int = DEFAULT_BUFFER_SIZE) :
        DefaultPool<ReusableAsyncChunkedOutput>(
            capacity = capasity,
            new = { pool ->
                ReusableAsyncChunkedOutput(
                    autoFlushBuffer = autoFlushBuffer,
                    onRevert = { self -> pool.recycle(self) },
                )
            }
        ),
        Closeable {
        fun new(stream: AsyncOutput, closeStream: Boolean) =
//            AsyncChunkedOutput(
//                stream = stream, closeStream = closeStream
//            )
            borrow()
                .also {
                    it.reset(stream = stream, closeStream = closeStream)
                }

        override fun close() {
            pool.forEach {
                (it as ReusableAsyncChunkedOutput?)?.forceCloseBuffer()
            }
        }
    }

    internal fun forceCloseBuffer() {
        super.closeInternalBuffers()
    }

    override fun closeInternalBuffers() {
        // Do nothing
    }

    fun reset(stream: AsyncOutput, closeStream: Boolean) {
        this.stream = stream
        this.closeStream = closeStream
        closed = false
        finished = false
        buffer.clear()
    }

    override suspend fun asyncClose() {
        if (stream === NullAsyncOutput) {
            throw IllegalStateException("Output stream not set")
        }
        try {
            super.asyncClose()
        } finally {
            stream = NullAsyncOutput
            onRevert?.invoke(this)
        }
    }
}
