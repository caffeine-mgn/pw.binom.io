package pw.binom

import pw.binom.io.Closeable
import pw.binom.io.EOFException
import pw.binom.io.UTF8
import pw.binom.pool.ObjectPool

interface Input : Closeable {
    //    fun skip(length: Long): Long
    fun read(dest: ByteBuffer): Int
    fun readFully(dest: ByteBuffer): Int {
        val length = dest.remaining
        while (dest.remaining > 0) {
            read(dest)
        }
        return length
    }

//    fun read(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int
//
//    fun readFully(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int {
//        var off = offset
//        var len = length
//        while (len > 0) {
//            val t = read(data, off, len)
//            off += t
//            len -= t
//        }
//        return length
//    }
}

//fun Input.copyTo(output: Output, pool: ObjectPool<ByteDataBuffer>) {
//    val buf = pool.borrow()
//    try {
//        while (true) {
//            val len = read(buf, 0, buf.size)
//            if (len <= 0) {
//                break
//            }
//            if (len > buf.size)
//                throw RuntimeException()
//            output.write(buf, 0, len)
//        }
//        output.flush()
//    } finally {
//        pool.recycle(buf)
//    }
//}

//fun Input.copyTo(outputStream: Output, bufferSize: Int = DEFAULT_BUFFER_SIZE) {
//    val buf = ByteDataBuffer.alloc(bufferSize)
//    while (true) {
//        val len = read(buf, 0, buf.size)
//        if (len <= 0) {
//            break
//        }
//        if (len > buf.size)
//            throw RuntimeException()
//        outputStream.write(buf, 0, len)
//    }
//    outputStream.flush()
//}

fun Input.readUtf8Char(buffer: ByteBuffer): Char? {
    buffer.reset(0, 1)
    return if (read(buffer) == 0) {
        null
    } else {
        val firstByte = buffer[0]
        val size = UTF8.utf8CharSize(firstByte)
        if (size > 0) {
            buffer.reset(0, size)
            read(buffer)
        }
        UTF8.utf8toUnicode(firstByte, buffer)
    }
}

fun Input.readUUID(buffer: ByteBuffer) =
    UUID.create(
        mostSigBits = readLong(buffer),
        leastSigBits = readLong(buffer)
    )

/**
 * Reads string with format [4 bytes with string length][other bytes of utf-8 string]
 *
 * @param buffer temp buffer
 * @return result string
 */
fun Input.readUTF8String(buffer: ByteBuffer): String {
    val size = readInt(buffer)
    val sb = StringBuilder(size)
    repeat(size) {
        sb.append(readUtf8Char(buffer) ?: throw EOFException())
    }
    return sb.toString()
}


fun Input.readByte(buffer: ByteBuffer): Byte {
    buffer.reset(0, 1)
    readFully(buffer)
    return buffer[0]
}

fun Input.readInt(buffer: ByteBuffer): Int {
    buffer.reset(0, 4)
    readFully(buffer)
    return Int.fromBytes(buffer[0], buffer[1], buffer[2], buffer[3])
}

fun Input.readShort(buffer: ByteBuffer): Short {
    buffer.reset(0, 2)
    return Short.fromBytes(buffer[0], buffer[1])
}

fun Input.readLong(buffer: ByteBuffer): Long {
    buffer.reset(0, 8)
    readFully(buffer)
    return Long.fromBytes(buffer[0], buffer[1], buffer[2], buffer[3], buffer[4], buffer[5], buffer[6], buffer[7])
}

fun Input.copyTo(output: Output, pool: ObjectPool<ByteBuffer>): Long {
    var totalLength = 0L
    val buffer = pool.borrow()
    try {
        while (true) {
            buffer.clear()
            val length = read(buffer)
            if (length == 0)
                break
            totalLength += length.toLong()
            buffer.flip()
            output.write(buffer)
        }
    } finally {
        pool.recycle(buffer)
    }
    return totalLength
}

suspend fun Input.copyTo(output: AsyncOutput, pool: ObjectPool<ByteBuffer>): Long {
    var totalLength = 0L
    val buffer = pool.borrow()
    try {
        while (true) {
            buffer.clear()
            val length = read(buffer)
            if (length == 0) {
                break
            }
            totalLength += length.toLong()
            buffer.flip()
            output.write(buffer)
        }
    } finally {
        pool.recycle(buffer)
    }
    return totalLength
}

fun Input.readFloat(buffer: ByteBuffer) = Float.fromBits(readInt(buffer))
fun Input.readDouble(buffer: ByteBuffer) = Double.fromBits(readLong(buffer))