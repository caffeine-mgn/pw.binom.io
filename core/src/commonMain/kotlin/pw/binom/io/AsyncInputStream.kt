package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.asUTF8String
import pw.binom.fromBytes
import pw.binom.internal_readln
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal val numberArray = ByteArray(Long.SIZE_BYTES)

interface AsyncInputStream : AsyncCloseable {
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
    if (read(numberArray, 0, 1) != 1)
        throw EOFException()
    return numberArray[0]
}

suspend fun AsyncInputStream.readln(): String = internal_readln { read() }

suspend fun AsyncInputStream.readShort():Short {
    if (readFully(numberArray, 0, Short.SIZE_BYTES) != Short.SIZE_BYTES)
        throw EOFException()
    return Short.fromBytes(numberArray[0], numberArray[1])
}

suspend fun AsyncInputStream.readInt(): Int {
    if (readFully(numberArray, 0, Int.SIZE_BYTES) != Int.SIZE_BYTES)
        throw EOFException()
    return Int.fromBytes(numberArray[0], numberArray[1], numberArray[2], numberArray[3])
}

suspend fun AsyncInputStream.readLong(): Long {
    if (readFully(numberArray, 0, Long.SIZE_BYTES) != Long.SIZE_BYTES)
        throw EOFException()
    return Long.fromBytes(numberArray[0], numberArray[1], numberArray[2], numberArray[3], numberArray[4], numberArray[5], numberArray[6], numberArray[7])
}

suspend fun AsyncInputStream.readFloat() = Float.fromBits(readInt())
suspend fun AsyncInputStream.readDouble() = Double.fromBits(readLong())
suspend fun AsyncInputStream.readUTF8String(): String {
    val len = readInt()
    val data = ByteArray(len)
    readFully(data)
    return data.asUTF8String()
}

suspend fun AsyncInputStream.copyTo(outputStream: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE) {
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

suspend fun AsyncInputStream.copyTo(outputStream: AsyncOutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE) {
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