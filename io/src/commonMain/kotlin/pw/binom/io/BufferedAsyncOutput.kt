package pw.binom.io

interface BufferedAsyncOutput : AsyncOutput {
  val outputBufferSize: Int

  suspend fun writeBoolean(value: Boolean) {
    writeByte(if (value) 1 else 0)
  }

  suspend fun writeByteArray(value: ByteArray)
}
