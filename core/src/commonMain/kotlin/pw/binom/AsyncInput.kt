package pw.binom

import pw.binom.io.AsyncCloseable
import pw.binom.io.EOFException
import pw.binom.io.UTF8
import pw.binom.pool.ObjectPool

interface AsyncInput : AsyncCloseable {
    /**
     * Available Data size in bytes
     * @return available data in bytes. If returns value less 0 it's mean that size of available data is  unknown
     */
    val available: Int
//    suspend fun skip(length: Long): Long
//    suspend fun read(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int
//    suspend fun readFully(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int {
//        var off = offset
//        var len = length
//        while (len > 0) {
//            val t = read(data, off, len)
//            off += t
//            len -= t
//        }
//        return length
//    }

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

    override suspend fun close() {
        this@asyncInput.close()
    }
}

//suspend fun AsyncInput.copyTo(outputStream: Output, pool: ObjectPool<ByteDataBuffer>) {
//    val buf = pool.borrow()
//    try {
//        while (true) {
//            val len = read(buf, 0, buf.size)
//            if (len <= 0) {
//                break
//            }
//            outputStream.write(buf, 0, len)
//        }
//        outputStream.flush()
//    } finally {
//        pool.recycle(buf)
//    }
//}

//suspend fun AsyncInput.copyTo(outputStream: AsyncOutput, pool: ObjectPool<ByteDataBuffer>) {
//    val buf = pool.borrow()
//    try {
//        while (true) {
//            val len = read(buf, 0, buf.size)
//            if (len <= 0) {
//                break
//            }
//            outputStream.write(buf, 0, len)
//        }
//        outputStream.flush()
//    } finally {
//        pool.recycle(buf)
//    }
//}

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

suspend fun AsyncInput.copyTo(output: AsyncOutput, pool: ObjectPool<ByteBuffer>) {
    val buf = pool.borrow()
    while (true) {
        buf.clear()
        val s = read(buf)
        if (s == 0)
            break
        buf.flip()
        output.write(buf)
    }
}

suspend fun AsyncInput.copyTo(output: Output, pool: ObjectPool<ByteBuffer>) {
    val buf = pool.borrow()
    while (true) {
        buf.clear()
        val s = read(buf)
        if (s == 0)
            break
        buf.flip()
        output.write(buf)
    }
}