package pw.binom

import pw.binom.io.Closeable
import pw.binom.io.UTF8

interface Output : Closeable {
    fun write(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int
    fun flush()
}

fun Output.writeUtf8Char(value: Char) {
    val size = UTF8.unicodeToUtf8(value, tmp8)
    write(tmp8, 0, size)
}

fun Output.writeUTF8String(text: String) {
    writeInt(text.length)
    text.forEach {
        writeUtf8Char(it)
    }
}

fun Output.writeByte(value: Byte) {
    tmp8[0] = value
    write(tmp8, 0, 1)
}

fun Output.writeInt(value: Int) {
    value.dump(tmp8)
    write(tmp8, 0, 4)
}

fun Output.writeShort(value: Short) {
    value.dump(tmp8)
    write(tmp8, 0, 2)
}

fun Output.writeLong(value: Long) {
    value.dump(tmp8)
    write(tmp8)
}

inline fun Output.writeFloat(value: Float) {
    writeInt(value.toBits())
}

inline fun Output.writeDouble(value: Double) {
    writeLong(value.toBits())
}