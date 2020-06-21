package pw.binom

import pw.binom.io.AsyncCloseable
import pw.binom.io.EOFException
import pw.binom.io.UTF8
import pw.binom.pool.ObjectPool

interface AsyncInput : AsyncCloseable {
    suspend fun skip(length: Long): Long
    suspend fun read(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int
    suspend fun readFully(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int {
        var off = offset
        var len = length
        while (len > 0) {
            val t = read(data, off, len)
            off += t
            len -= t
        }
        return length
    }
}

fun Input.asyncInput() = object : AsyncInput {
    override suspend fun skip(length: Long): Long =
            this@asyncInput.skip(length)

    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int =
            this@asyncInput.read(data, offset, length)

    override suspend fun close() {
        this@asyncInput.close()
    }
}

suspend fun AsyncInput.copyTo(outputStream: Output, pool: ObjectPool<ByteDataBuffer>) {
    val buf = pool.borrow()
    try {
        while (true) {
            val len = read(buf, 0, buf.size)
            if (len <= 0) {
                break
            }
            outputStream.write(buf, 0, len)
        }
        outputStream.flush()
    } finally {
        pool.recycle(buf)
    }
}

suspend fun AsyncInput.copyTo(outputStream: AsyncOutput, pool: ObjectPool<ByteDataBuffer>) {
    val buf = pool.borrow()
    try {
        while (true) {
            val len = read(buf, 0, buf.size)
            if (len <= 0) {
                break
            }
            outputStream.write(buf, 0, len)
        }
        outputStream.flush()
    } finally {
        pool.recycle(buf)
    }
}

suspend fun AsyncInput.readUtf8Char() =
        if (read(tmp8, length = 1) == 0) {
            null
        } else {
            val firstByte = tmp8[0]
            val size = UTF8.utf8CharSize(firstByte)
            if (size > 0)
                read(tmp8, length = size)
            UTF8.utf8toUnicode(firstByte, tmp8)
        }

suspend fun AsyncInput.readUTF8String(): String {
    val size = readInt()
    val sb = StringBuilder(size)
    repeat(size) {
        sb.append(readUtf8Char() ?: throw EOFException())
    }
    return sb.toString()
}

suspend fun AsyncInput.readByte(): Byte {
    readFully(tmp8, 0, 1)
    return tmp8[0]
}

suspend fun AsyncInput.readInt(): Int {
    readFully(tmp8, 0, 4)
    return Int.fromBytes(tmp8[0], tmp8[1], tmp8[2], tmp8[3])
}

suspend fun AsyncInput.readShort(): Short {
    readFully(tmp8, 0, 2)
    return Short.fromBytes(tmp8[0], tmp8[1])
}

suspend fun AsyncInput.readLong(): Long {
    readFully(tmp8, 0, 2)
    return Long.fromBytes(tmp8[0], tmp8[1], tmp8[2], tmp8[3], tmp8[4], tmp8[5], tmp8[6], tmp8[7])
}

inline suspend fun AsyncInput.readFloat() = Float.fromBits(readInt())
inline suspend fun AsyncInput.readDouble() = Double.fromBits(readLong())