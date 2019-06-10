package pw.binom.io

interface AsyncReader : AsyncCloseable {
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

suspend fun AsyncReader.readln(): String {
    val sb = StringBuilder()
    while (true) {
        try {
            val r = read()
            if (r == 10.toChar())
                break
            if (r == 13.toChar())
                continue
            sb.append(r)
        } catch (e: EOFException) {
            continue
        }
    }
    return sb.toString()
}

suspend fun AsyncReader.readText(): String {
    val sb = StringBuilder()
    while (true) {
        try {
            sb.append(read())
        } catch (e: EOFException) {
            break
        }
    }
    return sb.toString()
}