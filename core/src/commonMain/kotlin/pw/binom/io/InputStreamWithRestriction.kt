package pw.binom.io

class InputStreamWithRestriction(val stream: InputStream, val length: Long) : InputStream {

    override fun skip(length: Long): Long {
        val max = this.length - this.readed
        if (max == 0L)
            return 0L
        val o = stream.skip(minOf(max, length))
        readed += o
        return o
    }

    override val available: Int
        get() {
            val max = this.length - this.readed
            if (max == 0L)
                return 0
            return when (val r = stream.available) {
                -1 -> -1
                0 -> 0
                else -> minOf(r, stream.available)
            }
        }

    private var readed = 0L
    override fun close() {
        stream.close()
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        val max = this.length - this.readed
        if (max == 0L)
            return -1
        val readed = stream.read(data, offset, minOf(max.toInt(), length))
        if (readed == -1)
            return readed
        this.readed += readed
        return readed
    }
}

/**
 * Returns current stream with reading restriction by length
 */
fun InputStream.maxRead(length: Long) = InputStreamWithRestriction(this, length)