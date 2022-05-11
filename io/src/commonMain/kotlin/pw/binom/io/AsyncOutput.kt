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
