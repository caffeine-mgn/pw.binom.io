package pw.binom.io

interface Reader : Closeable {
    /**
     * @throws EOFException throws when stream is done
     */
    fun read(): Char?

    fun read(data: CharArray, offset: Int = 0, length: Int = data.size - offset): Int{
        if (offset + length > data.size)
            throw IndexOutOfBoundsException()
        var i = 0
        while (i < length) {
            try {
                val c = read() ?: break
                data[offset + i++] = c
            } catch (e: EOFException) {
                return i
            }
        }
        return i
    }
}

fun Reader.asAsync() = object : AsyncReader {
    override suspend fun readChar(): Char? =
            this@asAsync.read()

    override suspend fun read(data: CharArray, offset: Int, length: Int): Int =
            this@asAsync.read(data, offset, length)

    override suspend fun asyncClose() {
        this@asAsync.close()
    }

}

fun Reader.readln(): String? {
    val sb = StringBuilder()
    var first = true
    while (true) {
        val r = read()
        if (r == null && first)
            return null
        first = false
        if (r == null)
            break

        if (r == 10.toChar())
            break
        if (r == 13.toChar())
            continue
        sb.append(r)
    }
    return sb.toString()
}

fun Reader.readText(): String {
    val sb = StringBuilder()
    while (true) {
        sb.append(read() ?: break)
    }
    return sb.toString()
}