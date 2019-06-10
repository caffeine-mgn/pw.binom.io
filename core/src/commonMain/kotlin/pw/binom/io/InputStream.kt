package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.asUTF8String
import pw.binom.fromBytes

interface InputStream : Closeable {
    fun read(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int

    /**
     * the number of bytes that can be read from this input stream without blocking.
     * returns -1 of not support available property
     */
    val available: Int
        get() = -1
}

fun InputStream.read(): Byte {
    val b = ByteArray(1)
    if (read(b, 0, 1) != 1)
        throw EOFException()
    return b[0]
}

fun InputStream.readln(): String {
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

fun InputStream.copyTo(outputStream: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE) {
    val buf = ByteArray(bufferSize)
    while (true) {
        val len = read(buf, 0, buf.size)
        if (len <= 0) {
            break
        }
        outputStream.write(buf, 0, len)
    }
    outputStream.flush()
}

suspend fun InputStream.copyTo(outputStream: AsyncOutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE) {
    val buf = ByteArray(bufferSize)
    while (true) {
        val len = read(buf, 0, buf.size)
        if (len <= 0) {
            break
        }
        outputStream.write(buf, 0, len)
    }
    outputStream.flush()
}

fun InputStream.readShort() =
        Short.fromBytes(read(), read())

fun InputStream.readInt() =
        Int.fromBytes(read(), read(), read(), read())

fun InputStream.readLong() =
        Long.fromBytes(read(), read(), read(), read(), read(), read(), read(), read())

fun InputStream.readFloat() = Float.fromBits(readInt())
fun InputStream.readDouble() = Double.fromBits(readLong())
fun InputStream.readUTF8String(): String {
    val len = readInt()
    val data = ByteArray(len)
    read(data)
    return data.asUTF8String()
}

fun InputStream.asAsync() = object : AsyncInputStream {
    override suspend fun close() {
        this@asAsync.close()
    }

    override suspend fun read(data: ByteArray, offset: Int, length: Int): Int =
            this@asAsync.read(data, offset, length)

}