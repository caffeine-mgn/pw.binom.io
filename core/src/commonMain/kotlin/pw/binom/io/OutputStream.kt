package pw.binom.io

import pw.binom.asUTF8ByteArray
import pw.binom.get
import pw.binom.internal_write
import pw.binom.internal_writeln

interface OutputStream : Closeable {
    fun write(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int
    fun flush()
}

fun OutputStream.write(text: String) = internal_write(text) { write(it) }

fun OutputStream.write(value: Byte): Boolean {
    numberArray[0] = value
    return write(numberArray, 0, 1) == 1
}

fun OutputStream.writeln(text: String) = internal_writeln(text) { write(it) }
fun OutputStream.writeln() = internal_writeln("") { write(it) }

fun OutputStream.writeShort(value: Short) {
    numberArray[0] = value[0]
    numberArray[1] = value[1]
    write(numberArray, 0, Short.SIZE_BYTES)
}

fun OutputStream.writeInt(value: Int) {
    numberArray[0] = value[0]
    numberArray[1] = value[1]
    numberArray[2] = value[2]
    numberArray[3] = value[3]
    write(numberArray, 0, Int.SIZE_BYTES)
}

fun OutputStream.writeLong(value: Long) {
    numberArray[0] = value[0]
    numberArray[1] = value[1]
    numberArray[2] = value[2]
    numberArray[3] = value[3]
    numberArray[4] = value[4]
    numberArray[5] = value[5]
    numberArray[6] = value[6]
    numberArray[7] = value[7]
    write(numberArray, 0, Long.SIZE_BYTES)
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