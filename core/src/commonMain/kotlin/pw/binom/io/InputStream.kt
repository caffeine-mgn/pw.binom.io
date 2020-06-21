package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.asUTF8String
import pw.binom.fromBytes
import pw.binom.internal_readln

@Deprecated("Use Input")
interface InputStream : Closeable {
    fun read(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int

    fun skip(length: Long): Long = 0L

    fun read(): Byte {
        val b = ByteArray(1)
        if (read(b, 0, 1) != 1)
            throw EOFException()
        return b[0]
    }

    /**
     * the number of bytes that can be read from this input stream without blocking.
     * returns -1 of not support available property
     */
    val available: Int
        get() = -1
}



fun InputStream.readln(): String = internal_readln { read() }

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

fun InputStream.readShort(): Short {
    read(numberArray, 0, Short.SIZE_BYTES)
    return Short.fromBytes(numberArray[0], numberArray[1])
}


fun InputStream.readInt(): Int {
    read(numberArray, 0, Int.SIZE_BYTES)
    return Int.fromBytes(numberArray[0], numberArray[1], numberArray[2], numberArray[3])
}

fun InputStream.readLong(): Long {
    read(numberArray, 0, Long.SIZE_BYTES)
    return Long.fromBytes(
            numberArray[0],
            numberArray[1],
            numberArray[2],
            numberArray[3],
            numberArray[4],
            numberArray[5],
            numberArray[6],
            numberArray[7]
    )
}

fun InputStream.readFloat() = Float.fromBits(readInt())
fun InputStream.readDouble() = Double.fromBits(readLong())
fun InputStream.readUTF8String(): String {
    val len = readInt()
    val data = ByteArray(len)
    read(data)
    return data.asUTF8String()
}

fun InputStream.asAsync() = object : AsyncInputStream {
    override suspend fun read(): Byte =
            this@asAsync.read()

    override suspend fun close() {
        this@asAsync.close()
    }

    override suspend fun read(data: ByteArray, offset: Int, length: Int): Int =
            this@asAsync.read(data, offset, length)

}