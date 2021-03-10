package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.EmptyAsyncInput
import pw.binom.empty
import pw.binom.io.AbstractAsyncBufferedInput

@Deprecated(message = "Will be removed")
internal class PooledAsyncBufferedInputDeprecated(bufferSize: Int) : AbstractAsyncBufferedInput() {
    override val buffer: ByteBuffer = ByteBuffer.alloc(bufferSize).empty()
    var currentStream: AsyncInput = EmptyAsyncInput

    fun reset() {
        currentStream = EmptyAsyncInput
        buffer.clear()
        buffer.empty()
    }

    override suspend fun fill() {
        buffer.clear()
        try {
            super.fill()
        } catch (e: Throwable) {
            buffer.empty()
            throw e
        }
    }

    override val stream: AsyncInput
        get() = currentStream

    override suspend fun asyncClose() {
        super.asyncClose()
        buffer.close()
    }
}