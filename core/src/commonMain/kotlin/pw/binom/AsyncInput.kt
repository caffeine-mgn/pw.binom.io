package pw.binom

import pw.binom.io.AsyncCloseable
import pw.binom.io.EOFException
import pw.binom.io.UTF8
import pw.binom.pool.ObjectPool

interface AsyncInput : AsyncCloseable {
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

suspend fun AsyncInput.readUtf8Char():Char?{
    tmp8.reset(0,1)
    return if (read(tmp8) == 0) {
        null
    } else {
        val firstByte = tmp8[0]
        val size = UTF8.utf8CharSize(firstByte)
        if (size > 0) {
            tmp8.reset(0,size)
            read(tmp8)
        }
        UTF8.utf8toUnicode(firstByte, tmp8)
    }
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
    tmp8.reset(0,1)
    readFully(tmp8)
    return tmp8[0]
}

suspend fun AsyncInput.readInt(): Int {
    tmp8.reset(0,4)
    readFully(tmp8)
    return Int.fromBytes(tmp8[0], tmp8[1], tmp8[2], tmp8[3])
}

suspend fun AsyncInput.readShort(): Short {
    tmp8.reset(0,2)
    readFully(tmp8)
    return Short.fromBytes(tmp8[0], tmp8[1])
}

suspend fun AsyncInput.readLong(): Long {
    tmp8.reset(0,8)
    readFully(tmp8)
    return Long.fromBytes(tmp8[0], tmp8[1], tmp8[2], tmp8[3], tmp8[4], tmp8[5], tmp8[6], tmp8[7])
}

suspend inline fun AsyncInput.readFloat() = Float.fromBits(readInt())
suspend inline fun AsyncInput.readDouble() = Double.fromBits(readLong())

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