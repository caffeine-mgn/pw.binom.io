package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE

class AsyncBufferedChannel(
    val channel: AsyncChannel,
    readBufferSize: Int = DEFAULT_BUFFER_SIZE,
    writeBufferSize: Int = DEFAULT_BUFFER_SIZE
) : AsyncChannel {

    private val inputBuf = bufferedInput(readBufferSize)
    private val outputBuf = bufferedOutput(writeBufferSize)

    override suspend fun asyncClose() {
        channel.asyncClose()
    }

    override suspend fun write(data: ByteBuffer) =
        outputBuf.write(data)

    override suspend fun flush() {
        outputBuf.flush()
    }

    override val available: Int
        get() = inputBuf.available

    override suspend fun read(dest: ByteBuffer) =
        inputBuf.read(dest)
}
