package pw.binom.io

class InternalChannel(val readBuffer: ByteBuffer, val writeBuffer: ByteBuffer) : Channel {
  override fun read(dest: ByteBuffer): DataTransferSize {
    val p = readBuffer.position
    readBuffer.flip()
    val r = readBuffer.read(dest)
    readBuffer.compact()
    if (r.isAvailable) {
      readBuffer.position = p - r.length
    }
    return r
  }

  override fun close() {
    TODO("Not yet implemented")
  }

  override fun write(data: ByteBuffer) =
    writeBuffer.write(data)


  override fun flush() {
    TODO("Not yet implemented")
  }
}
