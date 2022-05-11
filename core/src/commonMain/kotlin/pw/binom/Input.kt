package pw.binom

import pw.binom.io.*
import pw.binom.pool.ObjectPool

fun Input.readUtf8Char(buffer: ByteBuffer): Char? {
    buffer.reset(0, 1)
    return if (read(buffer) == 0) {
        null
    } else {
        val firstByte = buffer[0]
        val size = UTF8.getUtf8CharSize(firstByte) - 1
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

fun Input.copyTo(output: Output, buffer: ByteBuffer): Long {
    var totalLength = 0L
    while (true) {
        buffer.clear()
        val length = read(buffer)
        if (length == 0)
            break
        totalLength += length.toLong()
        buffer.flip()
        output.write(buffer)
    }
    return totalLength
}

fun Input.copyTo(output: Output, pool: ObjectPool<ByteBuffer>): Long {
    val buffer = pool.borrow()
    return try {
        copyTo(output, buffer)
    } finally {
        pool.recycle(buffer)
    }
}

fun Input.copyTo(output: Output, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long =
    ByteBuffer.alloc(bufferSize) { buffer ->
        copyTo(output, buffer)
    }

suspend fun Input.copyTo(output: AsyncOutput, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long =
    ByteBuffer.alloc(bufferSize) { buffer ->
        copyTo(output, buffer)
    }

suspend fun Input.copyTo(output: AsyncOutput, buffer: ByteBuffer): Long {
    var totalLength = 0L
    while (true) {
        buffer.clear()
        val length = read(buffer)
        if (length == 0) {
            break
        }
        totalLength += length.toLong()
        buffer.flip()
        while (buffer.remaining > 0) {
            output.write(buffer)
        }
    }
    return totalLength
}

suspend fun Input.copyTo(output: AsyncOutput, pool: ObjectPool<ByteBuffer>): Long {
    val buffer = pool.borrow()
    return try {
        copyTo(output, buffer)
    } finally {
        pool.recycle(buffer)
    }
}

fun Input.readFloat(buffer: ByteBuffer) = Float.fromBits(readInt(buffer))
fun Input.readDouble(buffer: ByteBuffer) = Double.fromBits(readLong(buffer))
