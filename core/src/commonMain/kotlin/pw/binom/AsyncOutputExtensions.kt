package pw.binom

import pw.binom.NullAsyncOutput.write
import pw.binom.io.*
import pw.binom.pool.using
import pw.binom.uuid.UUID

fun Output.asyncOutput() = object : AsyncOutput {
//    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int =
//            this@asyncOutput.write(data, offset, length)

    override suspend fun write(data: ByteBuffer): Int =
        this@asyncOutput.write(data)

    override suspend fun flush() {
        this@asyncOutput.flush()
    }

    override suspend fun asyncClose() {
        this@asyncOutput.close()
    }
}

suspend fun AsyncOutput.writeUtf8Char(value: Char, buffer: ByteBuffer) {
    buffer.clear()
    UTF8.unicodeToUtf8(value, buffer)
    buffer.flip()
    write(buffer)
}

suspend fun AsyncOutput.writeUtf8Char(value: Char, pool: ByteBufferPool) {
    pool.using { buffer ->
        writeUtf8Char(value = value, buffer = buffer)
    }
}

suspend fun AsyncOutput.writeUTF8String(value: String, buffer: ByteBuffer) {
    writeInt(value = value.length, buffer = buffer)
    value.forEach {
        writeUtf8Char(value = it, buffer = buffer)
    }
}

suspend fun AsyncOutput.writeUTF8String(value: String, pool: ByteBufferPool) {
    pool.using { buffer ->
        writeUTF8String(value = value, buffer = buffer)
    }
}

suspend fun AsyncOutput.writeUUID(value: UUID, buffer: ByteBuffer) {
    writeLong(value = value.mostSigBits, buffer)
    writeLong(value = value.leastSigBits, buffer)
}

suspend fun AsyncOutput.writeUUID(value: UUID, pool: ByteBufferPool) {
    pool.using { buffer ->
        writeUUID(value = value, buffer = buffer)
    }
}

suspend fun AsyncOutput.writeByte(value: Byte, buffer: ByteBuffer) {
    buffer.clear()
    buffer.put(value)
    buffer.flip()
    write(buffer)
}

suspend fun AsyncOutput.writeByte(value: Byte, pool: ByteBufferPool) {
    pool.using { buffer ->
        writeByte(value = value, buffer = buffer)
    }
}

suspend fun AsyncOutput.writeInt(value: Int, buffer: ByteBuffer) {
    buffer.clear()
    value.dump(buffer)
    buffer.flip()
    write(buffer)
}

suspend fun AsyncOutput.writeInt(value: Int, pool: ByteBufferPool) {
    pool.using { buffer ->
        writeInt(value = value, buffer = buffer)
    }
}

suspend fun AsyncOutput.writeFloat(value: Float, buffer: ByteBuffer) {
    writeInt(value = value.toBits(), buffer = buffer)
}

suspend fun AsyncOutput.writeFloat(value: Float, pool: ByteBufferPool) {
    writeInt(value = value.toBits(), pool = pool)
}

suspend fun AsyncOutput.writeDouble(value: Double, buffer: ByteBuffer) {
    writeLong(value = value.toBits(), buffer = buffer)
}

suspend fun AsyncOutput.writeDouble(value: Double, pool: ByteBufferPool) {
    writeLong(value = value.toBits(), pool = pool)
}

suspend fun AsyncOutput.writeShort(value: Short, buffer: ByteBuffer) {
    buffer.clear()
    value.dump(buffer)
    buffer.flip()
    write(buffer)
}

suspend fun AsyncOutput.writeShort(value: Short, pool: ByteBufferPool) {
    pool.using { buffer ->
        writeShort(value = value, buffer = buffer)
    }
}

suspend fun AsyncOutput.writeLong(value: Long, buffer: ByteBuffer) {
    buffer.clear()
    value.dump(buffer)
    buffer.flip()
    write(buffer)
}

suspend fun AsyncOutput.writeLong(value: Long, pool: ByteBufferPool) {
    pool.using { buffer ->
        writeLong(value = value, buffer = buffer)
    }
}

/**
 * Special AsyncOutput for drop all output passed to [write]
 */
object NullAsyncOutput : AsyncOutput {
    override suspend fun write(data: ByteBuffer): Int {
        val remaining = data.remaining
        data.empty()
        return remaining
    }

    override suspend fun asyncClose() {
        // Do nothing
    }

    override suspend fun flush() {
        // Do nothing
    }
}
