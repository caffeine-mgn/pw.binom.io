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

object NullInput : Input {
    override fun read(dest: ByteBuffer): Int = 0

    override fun close() {
    }
}
