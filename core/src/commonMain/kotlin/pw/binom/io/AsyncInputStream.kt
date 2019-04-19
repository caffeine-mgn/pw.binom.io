package pw.binom.io

interface AsyncInputStream : Closeable {
    suspend fun read(data: ByteArray, offset: Int, length: Int): Int
}

/**
 * Reads some bytes from an input stream and stores them into the buffer array [data].
 *
 * @param data the buffer into which the data is read.
 * @param offset an int specifying the offset into the data
 * @param length an int specifying the number of bytes to read
 */
suspend fun AsyncInputStream.readFully(data: ByteArray, offset: Int, length: Int): Int {
    var off = offset
    var len = length
    while (true) {
        val readed = read(data, off, len)
        off += readed
        len -= readed

        if (len == 0)
            return length
    }
}

suspend fun AsyncInputStream.readLn(): String {
    val sb = StringBuilder()
    val buf = ByteArray(1)
    while (true) {
        val r = read(buf, 0, 1)
        if (r == 0 || buf[0] == 13.toByte()) {
            return sb.toString()
        }

        if (buf[0] == 10.toByte()) {
            continue
        }
        sb.append(buf[0].toChar())
    }
}