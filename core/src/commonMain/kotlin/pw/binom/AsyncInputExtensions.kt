package pw.binom

import pw.binom.io.*
import pw.binom.pool.ObjectPool
import pw.binom.uuid.UUID

fun Input.asyncInput() = object : AsyncInput {
    override val available: Int
        get() = -1

    override suspend fun read(dest: ByteBuffer): Int =
        this@asyncInput.read(dest)

    override suspend fun asyncClose() {
        this@asyncInput.close()
    }
}

suspend fun AsyncInput.readUtf8Char(pool: ByteBufferProvider): Char? {
    pool.using { buffer ->
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
}

suspend fun AsyncInput.readUTF8String(pool: ByteBufferProvider): String {
    pool.using { buffer ->
        val size = readInt(buffer)
        val sb = StringBuilder(size)
        repeat(size) {
            sb.append(readUtf8Char(buffer) ?: throw EOFException())
        }
        return sb.toString()
    }
}

suspend fun AsyncInput.readByte(pool: ByteBufferProvider): Byte {
    pool.using { buffer ->
        buffer.reset(0, 1)
        readFully(buffer)
        buffer.flip()
        return buffer[0]
    }
}

suspend fun AsyncInput.readUUID(pool: ByteBufferProvider) =
    UUID.create(
        mostSigBits = readLong(pool),
        leastSigBits = readLong(pool)
    )

suspend fun AsyncInput.readInt(pool: ByteBufferProvider): Int {
    pool.using { buffer ->
        buffer.reset(0, 4)
        readFully(buffer)
        buffer.flip()
        return Int.fromBytes(buffer[0], buffer[1], buffer[2], buffer[3])
    }
}

suspend fun AsyncInput.readShort(pool: ByteBufferProvider): Short {
    pool.using { buffer ->
        buffer.reset(0, 2)
        readFully(buffer)
        buffer.flip()
        return Short.fromBytes(buffer[0], buffer[1])
    }
}

suspend fun AsyncInput.readLong(pool: ByteBufferProvider): Long = pool.using { buffer ->
    buffer.reset(position = 0, length = Long.SIZE_BYTES)
    readFully(buffer)
    buffer.flip()

    Long.fromBytes(
        buffer.getByte(),
        buffer.getByte(),
        buffer.getByte(),
        buffer.getByte(),
        buffer.getByte(),
        buffer.getByte(),
        buffer.getByte(),
        buffer.getByte(),
    )
}

suspend inline fun AsyncInput.readFloat(buffer: ByteBuffer) = Float.fromBits(readInt(buffer))
suspend inline fun AsyncInput.readDouble(buffer: ByteBuffer) = Double.fromBits(readLong(buffer))

/**
 * Copy date from [this] to [output]
 *
 * @receiver input
 * @param output output
 * @param bufferProvider buffer for coping data from [this] to [output]. Buffer will not close after data coped
 * @return size of copied data
 */
suspend fun AsyncInput.copyTo(output: AsyncOutput, bufferProvider: ByteBufferProvider): Long {
    val totalLength = bufferProvider.using { tempBuffer ->
        var totalLength = 0L
        while (true) {
            tempBuffer.clear()
            val length = read(tempBuffer)
            if (length == 0) {
                break
            }
            totalLength += length.toLong()
            tempBuffer.flip()
            output.writeFully(tempBuffer)
        }
        totalLength
    }
    return totalLength
}

suspend fun AsyncInput.copyTo(output: Output, tempBuffer: ByteBuffer): Long {
    var totalLength = 0L
    while (true) {
        tempBuffer.clear()
        val length = read(tempBuffer)
        if (length == 0) {
            break
        }
        totalLength += length.toLong()
        tempBuffer.flip()
        output.write(tempBuffer)
    }
    return totalLength
}

// /**
// * Copy date from [this] to [output]
// *
// * @receiver input
// * @param output output
// * @param pool for get temp coping buffer
// * @return size of copied data
// */
// suspend fun AsyncInput.copyTo(output: AsyncOutput, pool: ObjectPool<ByteBuffer>): Long {
//    val buffer = pool.borrow()
//    try {
//        return copyTo(output, buffer)
//    } finally {
//        pool.recycle(buffer)
//    }
// }

/**
 * Copy date from [this] to [output]
 *
 * @receiver input
 * @param output output
 * @param bufferSize coping buffer size
 * @return size of copied data
 */
suspend fun AsyncInput.copyTo(output: AsyncOutput, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long =
    ByteBuffer(bufferSize).use { buffer ->
        copyTo(output, buffer)
    }

/**
 * Copy date from [this] to [output]
 *
 * @receiver input
 * @param output output
 * @return size of copied data
 */
suspend fun AsyncInput.copyTo(output: Output, pool: ObjectPool<ByteBuffer>): Long {
    val buffer = pool.borrow()
    try {
        return copyTo(output, buffer)
    } finally {
        pool.recycle(buffer)
    }
}

suspend fun AsyncInput.copyTo(output: Output, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long =
    ByteBuffer(bufferSize).use { buffer ->
        copyTo(output, buffer)
    }

suspend fun AsyncInput.skipAll(bufferSkipSize: Int = DEFAULT_BUFFER_SIZE) {
    ByteBuffer(bufferSkipSize).use {
        skipAll(it)
    }
}

suspend fun AsyncInput.skipAll(buffer: ByteBuffer) {
    while (true) {
        buffer.clear()
        if (read(buffer) == 0) {
            break
        }
    }
}

object EmptyAsyncInput : AsyncInput {
    override val available: Int
        get() = 0

    override suspend fun read(dest: ByteBuffer): Int = 0

    override suspend fun asyncClose() {
        // Do nothing
    }
}
