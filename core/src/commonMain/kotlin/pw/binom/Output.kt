package pw.binom

import pw.binom.io.Closeable
import pw.binom.io.IOException
import pw.binom.io.UTF8

interface Output : Closeable {
    //    fun write(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int
    fun write(data: ByteBuffer): Int
    fun flush()
    fun writeFully(data: ByteBuffer) {
        while (data.remaining > 0) {
            val wrote = write(data)
            if (wrote <= 0) {
                throw IOException("Can't write data")
            }
        }
    }
}

fun Output.writeUtf8Char(buffer: ByteBuffer, value: Char) {
    buffer.clear()
    val size = UTF8.unicodeToUtf8(value, buffer)
    buffer.reset(0, size)
    write(buffer)
}

fun Output.writeUTF8String(buffer: ByteBuffer, text: String) {
    writeInt(buffer, text.length)
    text.forEach {
        writeUtf8Char(buffer, it)
    }
}

fun Output.writeByte(buffer: ByteBuffer, value: Byte) {
    buffer[0] = value
    buffer.reset(0, 1)
    write(buffer)
}

fun Output.writeBytes(pool: ByteBufferPool, value: ByteArray) {
    val buf = pool.borrow()
    try {
        writeBytes(buf, value)
    } finally {
        pool.recycle(buf)
    }
}

fun Output.writeBytes(buffer: ByteBuffer, value: ByteArray) {
    buffer.clear()
    var l = value.size
    while (l > 0) {
        buffer.write(value, value.size - l)
        buffer.flip()
        l -= write(buffer)
    }
}

fun Output.writeUUID(buffer: ByteBuffer, value: UUID) {
    writeLong(buffer, value.mostSigBits)
    writeLong(buffer, value.leastSigBits)
}

fun Output.writeInt(buffer: ByteBuffer, value: Int) {
    buffer.clear()
    value.dump(buffer)
    buffer.flip()
    write(buffer)
}

fun Output.writeShort(buffer: ByteBuffer, value: Short) {
    buffer.clear()
    value.dump(buffer)
    buffer.flip()
    write(buffer)
}

fun Output.writeLong(buffer: ByteBuffer, value: Long) {
    buffer.clear()
    value.dump(buffer)
    buffer.flip()
    write(buffer)
}

inline fun Output.writeFloat(buffer: ByteBuffer, value: Float) {
    writeInt(buffer, value.toBits())
}

inline fun Output.writeDouble(buffer: ByteBuffer, value: Double) {
    writeLong(buffer, value.toBits())
}