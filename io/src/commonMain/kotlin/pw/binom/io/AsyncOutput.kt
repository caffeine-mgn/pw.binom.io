package pw.binom.io

interface AsyncOutput : AsyncCloseable, AsyncFlushable {
    //    suspend fun write(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int
    suspend fun write(data: ByteBuffer): Int

    suspend fun writeFully(data: ByteBuffer): Int {
        var writeSize = 0
        while (data.remaining > 0) {
            val wrote = write(data)
            if (wrote <= 0) {
                throw IOException("Can't write data")
            }
            writeSize += wrote
        }
        return writeSize
    }
}

fun AsyncOutput.withCounter() = AsyncOutputWithWriteCounter(this)

class AsyncOutputWithWriteCounter(val stream: AsyncOutput) : AsyncOutput {
    var writedBytes = 0L

    override suspend fun write(data: ByteBuffer): Int {
        val r = stream.write(data)
        writedBytes += r
        return r
    }

    override suspend fun asyncClose() {
        stream.flush()
    }

    override suspend fun flush() {
        stream.flush()
    }
}

suspend fun AsyncOutput.writeByteArray(data: ByteArray, bufferProvider: ByteBufferProvider) {
    bufferProvider.using { buffer ->
        require(buffer.capacity > 0) { "Buffer capacity should be more than 0" }
        var cursor = 0
        while (cursor < data.size) {
            buffer.clear()
            val len = buffer.write(data, offset = cursor)
            if (len <= 0) {
                break
            }
            buffer.flip()
            writeFully(buffer)
            cursor += len
        }
    }
}
