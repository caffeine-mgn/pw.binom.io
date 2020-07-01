package pw.binom

import pw.binom.io.Closeable
import pw.binom.io.EOFException
import pw.binom.io.UTF8
import pw.binom.pool.ObjectPool

interface Input : Closeable {
    fun skip(length: Long): Long
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

fun Input.readUtf8Char(): Char? {
    tmp8.reset(0, 1)
    return if (read(tmp8) == 0) {
        null
    } else {
        val firstByte = tmp8[0]
        val size = UTF8.utf8CharSize(firstByte)
        if (size > 0) {
            tmp8.reset(0, size)
            read(tmp8)
        }
        UTF8.utf8toUnicode(firstByte, tmp8)
    }
}

fun Input.readUTF8String(): String {
    val size = readInt()
    val sb = StringBuilder(size)
    repeat(size) {
        sb.append(readUtf8Char() ?: throw EOFException())
    }
    return sb.toString()
}


fun Input.readByte(): Byte {
    tmp8.reset(0, 1)
    readFully(tmp8)
    return tmp8[0]
}

fun Input.readInt(): Int {
    tmp8.reset(0, 4)
    readFully(tmp8)
    return Int.fromBytes(tmp8[0], tmp8[1], tmp8[2], tmp8[3])
}

fun Input.readShort(): Short {
    tmp8.reset(0, 2)
    return Short.fromBytes(tmp8[0], tmp8[1])
}

fun Input.readLong(): Long {
    tmp8.reset(0, 8)
    readFully(tmp8)
    return Long.fromBytes(tmp8[0], tmp8[1], tmp8[2], tmp8[3], tmp8[4], tmp8[5], tmp8[6], tmp8[7])
}

suspend fun Input.copyTo(output: AsyncOutput, pool: ObjectPool<ByteBuffer>) {
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

fun Input.readFloat() = Float.fromBits(readInt())
fun Input.readDouble() = Double.fromBits(readLong())