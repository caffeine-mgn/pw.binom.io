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
        println("readByteArray-> reading ${dest.size}")
        var cursor = 0
        while (cursor < dest.size) {
            buffer.reset(0, minOf(dest.size - cursor, buffer.capacity))
            val len = read(buffer)
            println("readByteArray-> readed $len")
            if (len == 0) {
                throw EOFException("Read $cursor/${dest.size}, can't read ${dest.size - cursor}")
            }
            buffer.flip()
            println("readByteArray-> After read in buffer: ")
            buffer.forEach {
                print(it.toUByte().toString(16).padStart(2, '0'))
            }
            println()
            val cp = buffer.read(dest, offset = cursor)
            println("readByteArray-> reading ${dest.size}. cp=$cp")
            cursor += len
        }
        val str = dest.map { it.toUByte().toString(16).padStart(2, '0') }.joinToString(" ")
        println("readByteArray-> result: $str")
    }
}
