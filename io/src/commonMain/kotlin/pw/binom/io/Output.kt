package pw.binom.io

interface Output : Closeable {
    fun write(data: ByteBuffer): DataTransferSize
    fun flush()
    fun writeFully(data: ByteBuffer) {
        while (data.remaining > 0) {
            val wrote = write(data)
            if (wrote.isNotAvailable) {
                throw IOException("Can't write data")
            }
        }
    }
}

object NullOutput : Output {
    override fun write(data: ByteBuffer): DataTransferSize {
        val remaining = data.remaining
        data.empty()
        return DataTransferSize.ofSize(remaining)
    }

    override fun close() {
        // Do nothing
    }

    override fun flush() {
        // Do nothing
    }
}

fun Output.writeByteArray(data: ByteArray, bufferProvider: ByteBufferProvider) {
    if (data.isEmpty()) {
        return
    }
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
