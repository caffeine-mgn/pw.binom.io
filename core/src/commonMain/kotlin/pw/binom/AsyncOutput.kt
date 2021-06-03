package pw.binom

import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncFlushable
import pw.binom.io.IOException
import pw.binom.io.UTF8
import pw.binom.pool.ObjectPool

interface AsyncOutput : AsyncCloseable, AsyncFlushable {
    //    suspend fun write(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int
    suspend fun write(data: ByteBuffer): Int

    suspend fun writeFully(data: ByteBuffer) {
        while (data.remaining > 0) {
            val wrote = write(data)
            if (wrote <= 0) {
                throw IOException("Can't write data")
            }
        }
    }
}

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

//suspend fun Input.copyTo(output: AsyncOutput, pool: ObjectPool<ByteDataBuffer>) {
//    val buf = pool.borrow()
//    try {
//        while (true) {
//            val len = read(buf, 0, buf.size)
//            if (len <= 0) {
//                break
//            }
//            output.write(buf, 0, len)
//        }
//        output.flush()
//    } finally {
//        pool.recycle(buf)
//    }
//}

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
    }

    override suspend fun flush() {
    }

}