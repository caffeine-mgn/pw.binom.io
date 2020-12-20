package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.DEFAULT_BUFFER_SIZE

class AsyncBufferedChannel(
        val channel: AsyncChannel,
        readBufferSize: Int = DEFAULT_BUFFER_SIZE,
        writeBufferSize: Int = DEFAULT_BUFFER_SIZE
) : AsyncChannel {
//    override val input = channel.input.buffered(readBufferSize)
//    override val output = channel.output.buffered(writeBufferSize)

    private val inputBuf = bufferedInput(readBufferSize)
    private val outputBuf = bufferedOutput(writeBufferSize)

    override suspend fun asyncClose() {
        channel.asyncClose()
    }

    override suspend fun write(data: ByteBuffer): Int =
            outputBuf.write(data)

//    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int =
//            outputBuf.write(data, offset, length)

    override suspend fun flush() {
        outputBuf.flush()
    }

    override val available: Int
        get() = inputBuf.available

//    override suspend fun skip(length: Long): Long =
//            inputBuf.skip(length)

//    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int =
//            inputBuf.read(data, offset, length)

    override suspend fun read(dest: ByteBuffer): Int =
            inputBuf.read(dest)

}