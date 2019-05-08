package pw.binom.io

import pw.binom.asUTF8String
import pw.binom.fromBytes

interface AsyncInputStream : Closeable {
    suspend fun read(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int
}

/**
 * Reads some bytes from an input stream and stores them into the buffer array [data].
 *
 * @param data the buffer into which the data is read.
 * @param offset an int specifying the offset into the data
 * @param length an int specifying the number of bytes to read
 */
suspend fun AsyncInputStream.readFully(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int {
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

/**
 * Read one byte from stream and returns it
 */
suspend fun AsyncInputStream.read(): Byte {
    val data = ByteArray(1)
    if (read(data, 0, 1) != 1)
        throw EOFException()
    return data[0]
}

suspend fun AsyncInputStream.readln(): String {
    val sb = StringBuilder()
    val buf = ByteArray(1)
    while (true) {
        val r = read(buf, 0, 1)
        if (r == 0 || buf[0] == 10.toByte()) {
            return sb.toString()
        }

        if (buf[0] == 13.toByte()) {
            continue
        }
        sb.append(buf[0].toChar())
    }
}

suspend fun AsyncInputStream.readShort() =
        Short.fromBytes(read(), read())

suspend fun AsyncInputStream.readInt() =
        Int.fromBytes(read(), read(), read(), read())

suspend fun AsyncInputStream.readLong() =
        Long.fromBytes(read(), read(), read(), read(), read(), read(), read(), read())

suspend fun AsyncInputStream.readFloat() = Float.fromBits(readInt())
suspend fun AsyncInputStream.readDouble() = Double.fromBits(readLong())
suspend fun AsyncInputStream.readUTF8String(): String {
    val len = readInt()
    val data = ByteArray(len)
    readFully(data)
    return data.asUTF8String()
}

suspend fun AsyncInputStream.copyTo(outputStream: OutputStream, bufferSize: Int = 512) {
    val buffer = ByteArray(bufferSize)
    while (true) {
        val len = read(buffer)
        if (len <= 0) {
            break
        }
        outputStream.write(buffer, 0, len)
    }
    outputStream.flush()
}