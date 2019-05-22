package pw.binom.io

interface AsyncReader : Closeable {
    /**
     * @throws EOFException throws when stream is done
     */
    suspend fun read(): Char

    suspend fun read(data: CharArray, offset: Int = 0, length: Int = data.size - offset): Int
}

abstract class AbstractAsyncReader : AsyncReader {
    override suspend fun read(data: CharArray, offset: Int, length: Int): Int {
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