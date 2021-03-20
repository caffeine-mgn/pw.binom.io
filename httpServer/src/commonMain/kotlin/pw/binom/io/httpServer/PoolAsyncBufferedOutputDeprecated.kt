package pw.binom.io.httpServer

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.NullAsyncOutput
import pw.binom.io.AbstractAsyncBufferedAsciiWriter
import pw.binom.io.AbstractAsyncBufferedOutput

@Deprecated(message = "Will be removed")
class PoolAsyncBufferedOutputDeprecated(bufferSize: Int) : AbstractAsyncBufferedAsciiWriter(closeParent = false) {
    override val buffer: ByteBuffer = ByteBuffer.alloc(bufferSize)
    var currentStream: AsyncOutput = NullAsyncOutput

    override val output: AsyncOutput
        get() = currentStream

    fun reset() {
        currentStream = NullAsyncOutput
        buffer.clear()
    }

    override suspend fun asyncClose() {
        try {
            super.asyncClose()
        } finally {
            buffer.close()
        }
    }
}