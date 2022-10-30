package pw.binom.io

interface AsyncInput : AsyncCloseable {
    /**
     * Available Data size in bytes
     * @return Available data in bytes. If returns value less 0 it's mean that size of available data is unknown
     */
    val available: Int

    suspend fun read(dest: ByteBuffer): Int
    suspend fun readFully(dest: ByteBuffer): Int {
        val length = dest.remaining
        while (dest.remaining > 0) {
            val read = read(dest)
            if (read == 0 && dest.remaining > 0) {
                throw EOFException("Full message $length bytes, can't read ${dest.remaining} bytes")
            }
        }
        return length
    }
}

suspend fun AsyncInput.readByteArray(size: Int, bufferProvider: ByteBufferProvider): ByteArray {
    require(size >= 0) { "size should be more or equals 0" }
    val array = ByteArray(size)
    readByteArray(
        dest = array,
        bufferProvider = bufferProvider,
    )
    return array
}

suspend fun AsyncInput.readByteArray(dest: ByteArray, bufferProvider: ByteBufferProvider) {
    bufferProvider.using { buffer ->
        var cursor = 0
        while (cursor < dest.size) {
            buffer.reset(0, minOf(dest.size - cursor, buffer.capacity))
            val len = read(buffer)
            if (len == 0) {
                throw EOFException("Read $cursor/${dest.size}, can't read ${dest.size - cursor}")
            }
            buffer.flip()
            buffer.read(dest, offset = cursor)
            cursor += len
        }
    }
}
