package pw.binom.io

actual class ByteArrayInputStream actual constructor(val data: ByteArray, offset: Int, length: Int) : InputStream {

    init {
        if (data.size - offset < length)
            throw IndexOutOfBoundsException("Range [$offset, $offset + $length) out of bounds for length ${data.size}")
    }

    var cursor = offset
    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (length - offset < length)
            throw IndexOutOfBoundsException("Range [$offset, $offset + $length) out of bounds for length ${data.size}")

        var max = this.data.size - cursor
        if (max <= 0)
            return 0
        max = minOf(max, length)
        (offset..max).forEach { i ->
            data[i + offset] = this.data[i]
        }
        cursor += max
        return max
    }

    override fun close() {
        //NOP
    }
}