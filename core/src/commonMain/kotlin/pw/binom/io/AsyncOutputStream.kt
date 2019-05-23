package pw.binom.io

import pw.binom.asUTF8ByteArray
import pw.binom.get

interface AsyncOutputStream : Closeable {
    suspend fun write(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int
    suspend fun flush()
}

suspend fun AsyncOutputStream.write(value: Byte): Boolean {
    val data = ByteArray(1) { value }
    return write(data) == 1
}

suspend fun AsyncOutputStream.write(text: String) {
    write(text.asUTF8ByteArray())
}

suspend fun AsyncOutputStream.writeln(text: String) {
    write(text)
    write("\r\n")
}

suspend fun AsyncOutputStream.writeShort(value: Short) {
    write(value[0])
    write(value[1])
}

suspend fun AsyncOutputStream.writeInt(value: Int) {
    write(value[0])
    write(value[1])
    write(value[2])
    write(value[3])
}

suspend fun AsyncOutputStream.writeLong(value: Long) {
    write(value[0])
    write(value[1])
    write(value[2])
    write(value[3])
    write(value[4])
    write(value[5])
    write(value[6])
    write(value[7])
}

suspend fun AsyncOutputStream.writeFloat(value:Float){
    writeInt(value.toRawBits())
}

suspend fun AsyncOutputStream.writeDouble(value:Double){
    writeLong(value.toRawBits())
}

suspend fun AsyncOutputStream.writeUTF8String(value:String){
    writeInt(value.length)
    write(value.asUTF8ByteArray())
}

fun OutputStream.asAsync(): AsyncOutputStream = object : AsyncOutputStream {
    override fun close() {
        this@asAsync.close()
    }

    override suspend fun flush() {
        this@asAsync.flush()
    }

    override suspend fun write(data: ByteArray, offset: Int, length: Int): Int =
            this@asAsync.write(data, offset, length)
}