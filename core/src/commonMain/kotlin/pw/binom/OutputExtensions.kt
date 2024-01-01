package pw.binom

import pw.binom.io.ByteBuffer
import pw.binom.io.Output
import pw.binom.io.UTF8
import pw.binom.uuid.UUID

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
        buffer.write(
            data = value,
            offset = value.size - l,
            length = minOf(value.size - (value.size - l), buffer.remaining),
        )
        buffer.flip()
        val wrote = write(buffer)
        if (wrote <= 0) {
            throw RuntimeException("Can't write bytes")
        }
        l -= wrote
    }
}

fun Output.writeUUID(buffer: ByteBuffer, value: UUID) {
    writeLong(buffer, value.mostSigBits)
    writeLong(buffer, value.leastSigBits)
}

fun Output.writeInt(buffer: ByteBuffer, value: Int) {
    buffer.clear()
    value.toByteBuffer(buffer)
    buffer.flip()
    write(buffer)
}

fun Output.writeShort(buffer: ByteBuffer, value: Short) {
    buffer.clear()
    value.toByteBuffer(buffer)
    buffer.flip()
    write(buffer)
}

fun Output.writeLong(buffer: ByteBuffer, value: Long) {
    buffer.clear()
    value.toByteBuffer(buffer)
    buffer.flip()
    write(buffer)
}

inline fun Output.writeFloat(buffer: ByteBuffer, value: Float) {
    writeInt(buffer, value.toBits())
}

inline fun Output.writeDouble(buffer: ByteBuffer, value: Double) {
    writeLong(buffer, value.toBits())
}
