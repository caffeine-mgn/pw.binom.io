package pw.binom.io

interface Reader : Closeable {
    /**
     * @throws EOFException throws when stream is done
     */
    fun read(): Char?

    fun read(data: CharArray, offset: Int = 0, length: Int = data.size - offset): Int
}

abstract class AbstractReader : Reader {
    override fun read(data: CharArray, offset: Int, length: Int): Int {
        if (offset + length > data.size)
            throw IndexOutOfBoundsException()
        var i = 0
        while (i < length) {
            try {
                data[offset + i++] = read() ?: break
            } catch (e: EOFException) {
                return i
            }
        }
        return i
    }
}

fun Reader.asAsync() = object : AsyncReader {
    override suspend fun read(): Char? =
            this@asAsync.read()

    override suspend fun read(data: CharArray, offset: Int, length: Int): Int =
            this@asAsync.read(data, offset, length)

    override suspend fun close() {
        this@asAsync.close()
    }

}

fun Reader.readln(): String? {
    val sb = StringBuilder()
    var first = true
    while (true) {
        try {
            val r = read()

            if (r == 10.toChar())
                break
            if (r == 13.toChar())
                continue
            sb.append(r ?: break)
            first = false
        } catch (e: EOFException) {
            if (first)
                return null
            break
        }
    }
    if (first)
        return null
    return sb.toString()
}

fun Reader.readText(): String {
    val sb = StringBuilder()
    while (true) {
        try {
            sb.append(read() ?: break)
        } catch (e: EOFException) {
            break
        }
    }
    return sb.toString()
}