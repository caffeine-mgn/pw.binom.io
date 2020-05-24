package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE

class AsyncBufferedChannel(
        val channel: AsyncChannel,
        readBufferSize: Int = DEFAULT_BUFFER_SIZE,
        writeBufferSize: Int = DEFAULT_BUFFER_SIZE
) : AsyncChannel {
    override val input = channel.input.buffered(readBufferSize)
    override val output = channel.output.buffered(writeBufferSize)

    override suspend fun close() {
        channel.close()
    }

}