package pw.binom.io

class StringReader(data: String) : Reader {
    private var cursor = 0
    private var closed = false

    private val data = data.toCharArray()
    override fun read(): Char? {
        if (closed) {
            error("String Reader already was closed")
        }
        if (cursor >= data.size) {
            return null
        }
        return data[cursor++]
    }

    override fun read(data: CharArray, offset: Int, length: Int): DataTransferSize {
        if (offset + length > data.size) {
            throw IndexOutOfBoundsException()
        }
        val len = minOf(this.data.size - cursor, length)
        if (len == 0) {
            return DataTransferSize.EMPTY
        }
        this.data.copyInto(data, offset, cursor, cursor + len)
        cursor += len
        return DataTransferSize.ofSize(len)
    }

    override fun close() {
        if (closed) {
            error("String Reader already was closed")
        }
        closed = true
    }
}

fun String.asReader() = StringReader(this)
