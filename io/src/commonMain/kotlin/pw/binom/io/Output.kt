package pw.binom.io

interface Output : Closeable {
    fun write(data: ByteBuffer): Int
    fun flush()
    fun writeFully(data: ByteBuffer) {
        while (data.remaining > 0) {
            val wrote = write(data)
            if (wrote <= 0) {
                throw IOException("Can't write data")
            }
        }
    }
}

object NullOutput : Output {
    override fun write(data: ByteBuffer): Int {
        val remaining = data.remaining
        data.empty()
        return remaining
    }

    override fun close() {
        // Do nothing
    }

    override fun flush() {
        // Do nothing
    }
}
