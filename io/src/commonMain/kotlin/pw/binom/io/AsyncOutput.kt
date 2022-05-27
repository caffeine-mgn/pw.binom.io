package pw.binom.io

interface AsyncOutput : AsyncCloseable, AsyncFlushable {
    //    suspend fun write(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int
    suspend fun write(data: ByteBuffer): Int

    suspend fun writeFully(data: ByteBuffer) {
        while (data.remaining > 0) {
            val wrote = write(data)
            if (wrote <= 0) {
                throw IOException("Can't write data")
            }
        }
    }
}

suspend fun AsyncOutput.write(data: ByteArray, bufferProvider: ByteBufferProvider) {
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
