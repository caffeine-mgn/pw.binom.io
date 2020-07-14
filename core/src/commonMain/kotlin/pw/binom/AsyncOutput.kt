package pw.binom

import pw.binom.io.AsyncCloseable
import pw.binom.io.UTF8
import pw.binom.pool.ObjectPool

interface AsyncOutput : AsyncCloseable {
//    suspend fun write(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int
    suspend fun write(data: ByteBuffer): Int
    suspend fun flush()
}

fun Output.asyncOutput() = object : AsyncOutput {
//    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int =
//            this@asyncOutput.write(data, offset, length)

    override suspend fun write(data: ByteBuffer): Int =
            this@asyncOutput.write(data)

    override suspend fun flush() {
        this@asyncOutput.flush()
    }

    override suspend fun close() {
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

suspend fun AsyncOutput.writeUtf8Char(value: Char) {
    tmp8.clear()
    UTF8.unicodeToUtf8(value, tmp8)
    tmp8.flip()
    write(tmp8)
}

suspend fun AsyncOutput.writeUTF8String(text: String) {
    writeInt(text.length)
    text.forEach {
        writeUtf8Char(it)
    }
}

suspend fun AsyncOutput.writeByte(value: Byte) {
    tmp8.clear()
    tmp8.put(value)
    tmp8.flip()
    write(tmp8)
}

suspend fun AsyncOutput.writeInt(value: Int) {
    tmp8.clear()
    value.dump(tmp8)
    tmp8.flip()
    write(tmp8)
}

inline suspend fun AsyncOutput.writeFloat(value: Float) {
    writeInt(value.toBits())
}

inline suspend fun AsyncOutput.writeDouble(value: Double) {
    writeLong(value.toBits())
}

suspend fun AsyncOutput.writeShort(value: Short) {
    tmp8.clear()
    value.dump(tmp8)
    tmp8.flip()
    write(tmp8)
}

suspend fun AsyncOutput.writeLong(value: Long) {
    tmp8.clear()
    value.dump(tmp8)
    tmp8.flip()
    write(tmp8)
}