package pw.binom.io

import pw.binom.asUTF8ByteArray
import pw.binom.get

interface OutputStream : Closeable {
    fun write(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int
    fun flush()
}

fun OutputStream.write(text: String) {
    write(text.asUTF8ByteArray())
}

fun OutputStream.write(value: Byte): Boolean {
    val data = ByteArray(1) { value }
    return write(data) == 1
}

fun OutputStream.writeln(text: String) {
    write(text)
    write("\r\n")
}

fun OutputStream.writeShort(value: Short) {
    write(value[0])
    write(value[1])
}

fun OutputStream.writeInt(value: Int) {
    write(value[0])
    write(value[1])
    write(value[2])
    write(value[3])
}

fun OutputStream.writeLong(value: Long) {
    write(value[0])
    write(value[1])
    write(value[2])
    write(value[3])
    write(value[4])
    write(value[5])
    write(value[6])
    write(value[7])
}

fun OutputStream.writeFloat(value: Float) {
    writeInt(value.toRawBits())
}

fun OutputStream.writeDouble(value: Double) {
    writeLong(value.toRawBits())
}

fun OutputStream.writeUTF8String(value: String) {
    writeInt(value.length)
    write(value.asUTF8ByteArray())
}