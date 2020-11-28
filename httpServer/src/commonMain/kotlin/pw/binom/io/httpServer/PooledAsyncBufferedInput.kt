package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.empty
import pw.binom.io.AbstractAsyncBufferedInput

internal class PooledAsyncBufferedInput(bufferSize: Int) : AbstractAsyncBufferedInput() {
    override val buffer: ByteBuffer = ByteBuffer.alloc(bufferSize).empty()
    var currentStream: AsyncInput? = null

    fun reset() {
        currentStream = null
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
        get() = currentStream!!

    override suspend fun close() {
        super.close()
        buffer.close()
    }
}