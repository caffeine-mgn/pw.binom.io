package pw.binom.io

interface Input : Closeable {
    fun read(dest: ByteBuffer): Int
    fun readFully(dest: ByteBuffer): Int {
        val length = dest.remaining
        while (dest.remaining > 0 && dest.remaining > 0) {
            if (read(dest) == 0) {
                throw EOFException()
            }
        }
        return length
    }
}

fun Input.readByteArray(size: Int, bufferProvider: ByteBufferProvider): ByteArray {
    require(size >= 0) { "size should be more or equals 0" }
    val array = ByteArray(size)
    readByteArray(
        dest = array,
        bufferProvider = bufferProvider,
    )
    return array
}

fun Input.readByteArray(dest: ByteArray, bufferProvider: ByteBufferProvider) {
    if (dest.isEmpty()) {
        return
    }
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

object NullInput : Input {
    override fun read(dest: ByteBuffer): Int = 0

    override fun close() {
        // Do nothing
    }
}
