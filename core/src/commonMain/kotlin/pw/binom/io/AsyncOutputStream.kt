package pw.binom.io

import pw.binom.asUTF8ByteArray
import pw.binom.get
import pw.binom.internal_write
import pw.binom.internal_writeln
@Deprecated("Use AsyncOutput")
interface AsyncOutputStream : AsyncCloseable {
    suspend fun write(data: Byte): Boolean
    suspend fun write(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int
    suspend fun flush()
}

//suspend fun AsyncOutputStream.write(value: Byte): Boolean {
//    numberArray[0] = value
//    return write(numberArray, 0, 1) == 1
//}

suspend fun AsyncOutputStream.write(text: String) = internal_write(text) { write(it) }

suspend fun AsyncOutputStream.writeln(text: String) = internal_writeln(text) { write(it) }
suspend fun AsyncOutputStream.writeln() = internal_writeln("") { write(it) }

suspend fun AsyncOutputStream.writeShort(value: Short) {
    numberArray[0] = value[0]
    numberArray[1] = value[1]
    write(numberArray, 0, Short.SIZE_BYTES)
}

suspend fun AsyncOutputStream.writeInt(value: Int) {
    numberArray[0] = value[0]
    numberArray[1] = value[1]
    numberArray[2] = value[2]
    numberArray[3] = value[3]
    write(numberArray, 0, Int.SIZE_BYTES)
}

suspend fun AsyncOutputStream.writeLong(value: Long) {
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

suspend fun AsyncOutputStream.writeFloat(value: Float) {
    writeInt(value.toRawBits())
}

suspend fun AsyncOutputStream.writeDouble(value: Double) {
    writeLong(value.toRawBits())
}

suspend fun AsyncOutputStream.writeUTF8String(value: String) {
    writeInt(value.length)
    write(value.asUTF8ByteArray())
}

fun OutputStream.asAsync(): AsyncOutputStream = object : AsyncOutputStream {
    override suspend fun write(data: Byte): Boolean =
            this@asAsync.write(data)

    override suspend fun close() {
        this@asAsync.close()
    }

    override suspend fun flush() {
        this@asAsync.flush()
    }

    override suspend fun write(data: ByteArray, offset: Int, length: Int): Int =
            this@asAsync.write(data, offset, length)
}

class AsyncNoCloseWrapperOutputStream(val stream: AsyncOutputStream) : AsyncOutputStream {
    override suspend fun write(data: Byte): Boolean {
        checkClosed()
        return stream.write(data)
    }

    override suspend fun write(data: ByteArray, offset: Int, length: Int): Int {
        checkClosed()
        return stream.write(data, offset, length)
    }

    override suspend fun flush() {
        checkClosed()
        stream.flush()
    }

    var closed: Boolean = false
        private set

    private fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    override suspend fun close() {
        checkClosed()
        closed = true
    }
}

fun AsyncOutputStream.noCloseWrapper() =
        when (this) {
            is AsyncNoCloseWrapperOutputStream -> this
            else -> AsyncNoCloseWrapperOutputStream(this)
        }