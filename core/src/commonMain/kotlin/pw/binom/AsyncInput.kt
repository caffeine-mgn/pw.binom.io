package pw.binom

import pw.binom.io.AsyncCloseable
import pw.binom.io.EOFException
import pw.binom.io.UTF8
import pw.binom.pool.ObjectPool

interface AsyncInput : AsyncCloseable {
    /**
     * Available Data size in bytes
     * @return Available data in bytes. If returns value less 0 it's mean that size of available data is unknown
     */
    val available: Int

    suspend fun read(dest: ByteBuffer): Int
    suspend fun readFully(dest: ByteBuffer): Int {
        val length = dest.remaining
        while (dest.remaining > 0) {
            read(dest)
        }
        return length
    }
}

fun Input.asyncInput() = object : AsyncInput {
    override val available: Int
        get() = -1

    override suspend fun read(dest: ByteBuffer): Int =
        this@asyncInput.read(dest)

    override suspend fun asyncClose() {
        this@asyncInput.close()
    }
}

suspend fun AsyncInput.readUtf8Char(buffer: ByteBuffer): Char? {
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


suspend fun AsyncInput.readUTF8String(buffer: ByteBuffer): String {
    val size = readInt(buffer)
    val sb = StringBuilder(size)
    repeat(size) {
        sb.append(readUtf8Char(buffer) ?: throw EOFException())
    }
    return sb.toString()
}

suspend fun AsyncInput.readByte(buffer: ByteBuffer): Byte {
    buffer.reset(0, 1)
    readFully(buffer)
    buffer.flip()
    return buffer[0]
}

suspend fun AsyncInput.readUUID(buffer: ByteBuffer) =
    UUID.create(
        mostSigBits = readLong(buffer),
        leastSigBits = readLong(buffer)
    )

suspend fun AsyncInput.readInt(buffer: ByteBuffer): Int {
    buffer.reset(0, 4)
    readFully(buffer)
    buffer.flip()
    return Int.fromBytes(buffer[0], buffer[1], buffer[2], buffer[3])
}

suspend fun AsyncInput.readShort(buffer: ByteBuffer): Short {
    buffer.reset(0, 2)
    readFully(buffer)
    buffer.flip()
    return Short.fromBytes(buffer[0], buffer[1])
}

suspend fun AsyncInput.readLong(buffer: ByteBuffer): Long {
    buffer.reset(0, 8)
    readFully(buffer)
    buffer.flip()
    return Long.fromBytes(buffer[0], buffer[1], buffer[2], buffer[3], buffer[4], buffer[5], buffer[6], buffer[7])
}

suspend inline fun AsyncInput.readFloat(buffer: ByteBuffer) = Float.fromBits(readInt(buffer))
suspend inline fun AsyncInput.readDouble(buffer: ByteBuffer) = Double.fromBits(readLong(buffer))

/**
 * Copy date from [this] to [output]
 *
 * @receiver input
 * @param output output
 * @param tempBuffer buffer for coping data from [this] to [output]. Buffer will not close after data coped
 * @return size of copied data
 */
suspend fun AsyncInput.copyTo(output: AsyncOutput, tempBuffer: ByteBuffer): Long {
    var totalLength = 0L
    while (true) {
        tempBuffer.clear()
        val length = read(tempBuffer)
        if (length == 0)
            break
        totalLength += length.toLong()
        tempBuffer.flip()
        output.write(tempBuffer)
    }
    return totalLength
}

/**
 * Copy date from [this] to [output]
 *
 * @receiver input
 * @param output output
 * @param pool for get temp coping buffer
 * @return size of copied data
 */
suspend fun AsyncInput.copyTo(output: AsyncOutput, pool: ObjectPool<ByteBuffer>): Long {
    var totalLength = 0L
    val buffer = pool.borrow()
    try {
        copyTo(output, buffer)
    } finally {
        pool.recycle(buffer)
    }
    return totalLength
}

/**
 * Copy date from [this] to [output]
 *
 * @receiver input
 * @param output output
 * @param bufferSize coping buffer size
 * @return size of copied data
 */
suspend fun AsyncInput.copyTo(output: AsyncOutput, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
    var totalLength = 0L
    val buffer = ByteBuffer.alloc(bufferSize)
    try {
        copyTo(output, buffer)
    } finally {
        buffer.close()
    }
    return totalLength
}

/**
 * Copy date from [this] to [output]
 *
 * @receiver input
 * @param output output
 * @return size of copied data
 */
suspend fun AsyncInput.copyTo(output: Output, pool: ObjectPool<ByteBuffer>): Long {
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

suspend fun AsyncInput.skipAll(buffer: ByteBuffer) {
    while (true) {
        buffer.clear()
        if (read(buffer) == 0)
            break
    }
}

object EmptyAsyncInput : AsyncInput {
    override val available: Int
        get() = 0

    override suspend fun read(dest: ByteBuffer): Int = 0

    override suspend fun asyncClose() {
    }

}