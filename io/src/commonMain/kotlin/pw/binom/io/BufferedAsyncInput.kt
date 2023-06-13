package pw.binom.io

interface BufferedAsyncInput : AsyncInput {
    val inputBufferSize: Int

    suspend fun readByte(): Byte
    suspend fun readShort(): Short
    suspend fun readInt(): Int
    suspend fun readLong(): Long
    suspend fun read(dest: ByteArray, offset: Int = 0, length: Int = dest.size - offset): Int
    suspend fun readFully(dest: ByteArray, offset: Int = 0, length: Int = dest.size - offset): Int
}
