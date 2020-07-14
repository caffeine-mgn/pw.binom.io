package pw.binom.io.httpServer

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.io.AbstractAsyncBufferedOutput

class PoolAsyncBufferedOutput(bufferSize: Int) : AbstractAsyncBufferedOutput() {
    override val buffer: ByteBuffer = ByteBuffer.alloc(bufferSize)
    var currentStream: AsyncOutput? = null
    override val stream: AsyncOutput
        get() = currentStream!!

    fun reset() {
        currentStream = null
        buffer.clear()
    }

    override suspend fun close() {
        super.close()
        buffer.close()
    }
}