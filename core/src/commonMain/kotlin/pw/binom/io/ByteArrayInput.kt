package pw.binom.io

class ByteArrayInput(val data: ByteArray) : Input {
    private var closed = false
    private var cursor = 0
    override fun read(dest: ByteBuffer): Int {
        checkClosed()
        val max = minOf(data.size - cursor, dest.remaining123)
        if (max == 0) {
            return 0
        }
        dest.write(
            data = data,
            offset = cursor,
            length = max,
        )
        cursor += max
        return max
    }

    private fun checkClosed() {
        if (closed) {
            throw ClosedException()
        }
    }

    override fun close() {
        checkClosed()
        closed = true
    }
}
