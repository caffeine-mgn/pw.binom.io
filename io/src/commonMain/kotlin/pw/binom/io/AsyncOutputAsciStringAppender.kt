package pw.binom.io

class AsyncOutputAsciStringAppender : AsyncOutput {
  private val sb = StringBuilder()

  fun clear() {
    sb.clear()
  }

  fun trimToSize() {
    sb.trimToSize()
  }

  val length
    get() = sb.length

  fun ensureCapacity(minimumCapacity: Int) {
    sb.ensureCapacity(minimumCapacity)
  }

  override suspend fun write(data: ByteBuffer): DataTransferSize {
    val len = data.remaining
    if (len == 0) {
      return DataTransferSize.EMPTY
    }
    sb.ensureCapacity(sb.length + len)
    data.forEach { byte ->
      sb.append(byte.toInt().toChar())
    }
    data.position = data.limit
    return DataTransferSize.ofSize(len)
  }

  override suspend fun asyncClose() {
  }

  override suspend fun flush() {
  }

  override fun toString(): String = sb.toString()
}
