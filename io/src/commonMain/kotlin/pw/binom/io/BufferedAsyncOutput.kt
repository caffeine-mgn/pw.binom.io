package pw.binom.io

interface BufferedAsyncOutput : AsyncOutput {
  val outputBufferSize: Int

  suspend fun writeBoolean(value: Boolean) {
    writeByte(if (value) 1 else 0)
  }

  override suspend fun writeByte(value: Byte)
  suspend fun writeShort(value: Short)
  suspend fun writeInt(value: Int)
  suspend fun writeLong(value: Long)
  suspend fun writeFloat(value: Float) {
    writeInt(value.toBits())
  }

  suspend fun writeDouble(value: Double) {
    writeLong(value.toBits())
  }

  suspend fun writeByteArray(value: ByteArray)
  suspend fun writeString(value: String) {
    val bytes = value.encodeToByteArray()
    writeInt(bytes.size)
    writeByteArray(bytes)
  }
}
