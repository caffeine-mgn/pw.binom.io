package pw.binom.io

interface BufferedAsyncInput : AsyncInput {
  val inputBufferSize: Int


  suspend fun readBoolean() = readByte() > 0
  suspend fun readByte(): Byte
  suspend fun readShort(): Short
  suspend fun readInt(): Int
  suspend fun readLong(): Long
  suspend fun readFloat() = Float.fromBits(readInt())
  suspend fun readDouble() = Double.fromBits(readLong())
  suspend fun readString(): String {
    val size = readInt()
    val bytes = ByteArray(size)
    readFully(bytes)
    return bytes.decodeToString()
  }

  suspend fun read(dest: ByteArray, offset: Int = 0, length: Int = dest.size - offset): Int
  suspend fun readFully(dest: ByteArray, offset: Int = 0, length: Int = dest.size - offset): Int
}
