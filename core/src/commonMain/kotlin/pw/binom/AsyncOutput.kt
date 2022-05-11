package pw.binom

import pw.binom.NullAsyncOutput.write
import pw.binom.io.*

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

suspend fun AsyncOutput.writeUtf8Char(buffer: ByteBuffer, value: Char) {
    buffer.clear()
    UTF8.unicodeToUtf8(value, buffer)
    buffer.flip()
    write(buffer)
}

suspend fun AsyncOutput.writeUTF8String(buffer: ByteBuffer, text: String) {
    writeInt(buffer, text.length)
    text.forEach {
        writeUtf8Char(buffer, it)
    }
}

suspend fun AsyncOutput.writeUUID(buffer: ByteBuffer, value: UUID) {
    writeLong(buffer, value.mostSigBits)
    writeLong(buffer, value.leastSigBits)
}

suspend fun AsyncOutput.writeByte(buffer: ByteBuffer, value: Byte) {
    buffer.clear()
    buffer.put(value)
    buffer.flip()
    write(buffer)
}

suspend fun AsyncOutput.writeInt(buffer: ByteBuffer, value: Int) {
    buffer.clear()
    value.dump(buffer)
    buffer.flip()
    write(buffer)
}

suspend fun AsyncOutput.writeFloat(buffer: ByteBuffer, value: Float) {
    writeInt(buffer, value.toBits())
}

suspend fun AsyncOutput.writeDouble(buffer: ByteBuffer, value: Double) {
    writeLong(buffer, value.toBits())
}

suspend fun AsyncOutput.writeShort(buffer: ByteBuffer, value: Short) {
    buffer.clear()
    value.dump(buffer)
    buffer.flip()
    write(buffer)
}

suspend fun AsyncOutput.writeLong(buffer: ByteBuffer, value: Long) {
    buffer.clear()
    value.dump(buffer)
    buffer.flip()
    write(buffer)
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
