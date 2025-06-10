package pw.binom.io

class AsciiInputReader(val input: Input) : Reader {
  private val buffer = ByteBuffer(1)
  override fun read(): Char? {
    buffer.clear()
    val r = input.read(buffer)
    if (r.isNotAvailable) {
      return null
    }
    buffer.flip()
    return buffer.getByte().toInt().toChar()
  }

  override fun close() {
    input.close()
    buffer.close()
  }
}
