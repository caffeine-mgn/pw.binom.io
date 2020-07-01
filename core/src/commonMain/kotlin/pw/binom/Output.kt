package pw.binom

import pw.binom.io.Closeable
import pw.binom.io.UTF8

interface Output : Closeable {
//    fun write(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int
    fun write(data: ByteBuffer): Int
    fun flush()
}

fun Output.writeUtf8Char(value: Char) {
    val size = UTF8.unicodeToUtf8(value, tmp8)
    tmp8.reset(0,size)
    write(tmp8)
}

fun Output.writeUTF8String(text: String) {
    writeInt(text.length)
    text.forEach {
        writeUtf8Char(it)
    }
}

fun Output.writeByte(value: Byte) {
    tmp8[0] = value
    tmp8.reset(0,1)
    write(tmp8)
}

fun Output.writeInt(value: Int) {
    tmp8.clear()
    value.dump(tmp8)
    tmp8.flip()
    write(tmp8)
}

fun Output.writeShort(value: Short) {
    tmp8.clear()
    value.dump(tmp8)
    tmp8.flip()
    write(tmp8)
}

fun Output.writeLong(value: Long) {
    tmp8.clear()
    value.dump(tmp8)
    tmp8.flip()
    write(tmp8)
}

inline fun Output.writeFloat(value: Float) {
    writeInt(value.toBits())
}

inline fun Output.writeDouble(value: Double) {
    writeLong(value.toBits())
}