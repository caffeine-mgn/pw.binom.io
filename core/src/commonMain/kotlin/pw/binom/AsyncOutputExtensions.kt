package pw.binom

import pw.binom.NullAsyncOutput.write
import pw.binom.io.*
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

suspend fun AsyncOutput.writeUtf8Char(pool: ByteBufferProvider, value: Char) {
    pool.using { buffer ->
        buffer.clear()
        UTF8.unicodeToUtf8(value, buffer)
        buffer.flip()
        write(buffer)
    }
}

suspend fun AsyncOutput.writeUTF8String(pool: ByteBufferProvider, text: String) {
    writeInt(pool, text.length)
    text.forEach {
        writeUtf8Char(pool, it)
    }
}

suspend fun AsyncOutput.writeUUID(pool: ByteBufferProvider, value: UUID) {
    writeLong(pool, value.mostSigBits)
    writeLong(pool, value.leastSigBits)
}

suspend fun AsyncOutput.writeByte(pool: ByteBufferProvider, value: Byte) {
    pool.using { buffer ->
        buffer.clear()
        buffer.put(value)
        buffer.flip()
        write(buffer)
    }
}

suspend fun AsyncOutput.writeInt(pool: ByteBufferProvider, value: Int) {
    pool.using { buffer ->
        buffer.clear()
        value.dump(buffer)
        buffer.flip()
        write(buffer)
    }
}

suspend fun AsyncOutput.writeFloat(pool: ByteBufferProvider, value: Float) {
    writeInt(pool, value.toBits())
}

suspend fun AsyncOutput.writeDouble(pool: ByteBufferProvider, value: Double) {
    writeLong(pool, value.toBits())
}

suspend fun AsyncOutput.writeShort(pool: ByteBufferProvider, value: Short) {
    pool.using { buffer ->
        buffer.clear()
        value.dump(buffer)
        buffer.flip()
        write(buffer)
    }
}

suspend fun AsyncOutput.writeLong(pool: ByteBufferProvider, value: Long) {
    pool.using { buffer ->
        buffer.clear()
        value.dump(buffer)
        buffer.flip()
        write(buffer)
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
