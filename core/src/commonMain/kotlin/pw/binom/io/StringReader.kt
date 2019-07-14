package pw.binom.io

class StringReader(private val data: String) : Reader {
    private var cursor = 0
    private var closed = false

    override fun read(): Char? {
        if (closed)
            throw IllegalStateException("String Reader already was closed")
        if (cursor >= data.length)
            return null
        return data[cursor++]
    }

    override fun read(data: CharArray, offset: Int, length: Int): Int {
        if (offset + length > data.size)
            throw IndexOutOfBoundsException()
        var i = 0
        while (cursor < this.data.length && i < length) {
            data[offset + i++] = read() ?: break
        }
        return i
    }

    override fun close() {
        if (closed)
            throw IllegalStateException("String Reader already was closed")
        closed = true
    }
}

fun String.asReader() = StringReader(this)