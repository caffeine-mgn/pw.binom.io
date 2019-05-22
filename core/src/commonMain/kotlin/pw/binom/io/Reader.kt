package pw.binom.io

interface Reader : Closeable {
    /**
     * @throws EOFException throws when stream is done
     */
    fun read(): Char

    fun read(data: CharArray, offset: Int = 0, length: Int = data.size - offset): Int
}

abstract class AbstractReader : Reader {
    override fun read(data: CharArray, offset: Int, length: Int): Int {
        if (offset + length > data.size)
            throw IndexOutOfBoundsException()
        var i = 0
        while (i < length) {
            try {
                data[offset + i++] = read()
            } catch (e: EOFException) {
                return i
            }
        }
        return i
    }
}

fun Reader.asAsync() = object : AsyncReader {
    override suspend fun read(): Char =
            this@asAsync.read()

    override suspend fun read(data: CharArray, offset: Int, length: Int): Int =
            this@asAsync.read(data, offset, length)

    override fun close() {
        this@asAsync.close()
    }

}