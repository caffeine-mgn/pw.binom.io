package pw.binom

import pw.binom.io.AsyncCloseable
import pw.binom.io.UTF8
import pw.binom.pool.ObjectPool

interface AsyncOutput : AsyncCloseable {
    suspend fun write(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int
    suspend fun flush()
}

fun Output.asyncOutput() = object : AsyncOutput {
    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int =
            this@asyncOutput.write(data, offset, length)

    override suspend fun flush() {
        this@asyncOutput.flush()
    }

    override suspend fun close() {
        this@asyncOutput.close()
    }
}

suspend fun Input.copyTo(output: AsyncOutput, pool: ObjectPool<ByteDataBuffer>) {
    val buf = pool.borrow()
    try {
        while (true) {
            val len = read(buf, 0, buf.size)
            if (len <= 0) {
                break
            }
            output.write(buf, 0, len)
        }
        output.flush()
    } finally {
        pool.recycle(buf)
    }
}

suspend fun AsyncOutput.writeUtf8Char(value: Char) {
    val size = UTF8.unicodeToUtf8(value, tmp8)
    write(tmp8, 0, size)
}

suspend fun AsyncOutput.writeUTF8String(text: String) {
    writeInt(text.length)
    text.forEach {
        writeUtf8Char(it)
    }
}

suspend fun AsyncOutput.writeByte(value: Byte) {
    tmp8[0] = value
    write(tmp8, 0, 1)
}

suspend fun AsyncOutput.writeInt(value: Int) {
    value.dump(tmp8)
    write(tmp8, 0, 4)
}

inline suspend fun AsyncOutput.writeFloat(value: Float) {
    writeInt(value.toBits())
}

inline suspend fun AsyncOutput.writeDouble(value: Double) {
    writeLong(value.toBits())
}

suspend fun AsyncOutput.writeShort(value: Short) {
    value.dump(tmp8)
    write(tmp8, 0, 2)
}

suspend fun AsyncOutput.writeLong(value: Long) {
    value.dump(tmp8)
    write(tmp8)
}