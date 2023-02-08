package pw.binom.io.http

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.NullAsyncOutput
import pw.binom.io.AsyncOutput
import pw.binom.pool.ObjectFactory
import pw.binom.pool.ObjectPool

open class ReusableAsyncChunkedOutput(
    autoFlushBuffer: Int = DEFAULT_BUFFER_SIZE,
    var onRevert: ((ReusableAsyncChunkedOutput) -> Unit)? = null
) : AsyncChunkedOutput(
    stream = NullAsyncOutput,
    closeStream = false,
    autoFlushBuffer = autoFlushBuffer,
) {

    class Factory(val autoFlushBuffer: Int = DEFAULT_BUFFER_SIZE) : ObjectFactory<ReusableAsyncChunkedOutput> {
        override fun allocate(pool: ObjectPool<ReusableAsyncChunkedOutput>): ReusableAsyncChunkedOutput =
            ReusableAsyncChunkedOutput(
                autoFlushBuffer = autoFlushBuffer,
                onRevert = { self -> pool.recycle(self) },
            )

        override fun deallocate(value: ReusableAsyncChunkedOutput, pool: ObjectPool<ReusableAsyncChunkedOutput>) {
            value.forceCloseBuffer()
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
        closed.setValue(false)
        finished.setValue(false)
        buffer.clear()
    }

    override suspend fun asyncClose() {
        if (stream === NullAsyncOutput) {
            error("Output stream not set")
        }
        try {
            super.asyncClose()
        } finally {
            stream = NullAsyncOutput
            onRevert?.invoke(this)
        }
    }
}
