package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE

interface Input : Closeable {
  fun read(dest: ByteBuffer): DataTransferSize

  /**
   * Read data to [dest] until dest has any space.
   * If data is end before fill [dest] throws [EOFException]
   */
  @Throws(EOFException::class)
  fun readFully(dest: ByteBuffer): Int {
    if (dest.remaining == 0) {
      return 0
    }
    val length = dest.remaining
    while (dest.remaining > 0 && dest.remaining > 0) {
      if (read(dest).isNotAvailable) {
        throw EOFException()
      }
    }
    return length
  }

  fun skipAll(bufferSize: Int = DEFAULT_BUFFER_SIZE) {
    require(bufferSize > 0) { "Buffer size must be greater than or equal to zero." }
    ByteBuffer(bufferSize).use { buffer ->
      skipAll(buffer = buffer)
    }
  }

  fun skipAll(buffer: ByteBuffer) {
    require(buffer.capacity > 0) { "Buffer capacity must be greater than or equal to zero." }
    while (true) {
      buffer.clear()
      if (read(buffer).isNotAvailable) {
        break
      }
    }
  }

  @Throws(EOFException::class)
  fun skip(bytes: Long, bufferSize: Int = DEFAULT_BUFFER_SIZE) {
    if (bytes == 0L) {
      return
    }
    require(bytes >= 0) { "bytes must be greater than or equal to zero." }
    require(bufferSize > 0) { "Buffer size must be greater than or equal to zero." }
    ByteBuffer(bufferSize).use { buffer ->
      skip(bytes = bytes, buffer = buffer)
    }
  }

  @Throws(EOFException::class)
  fun skip(bytes: Long, buffer: ByteBuffer) {
    if (bytes == 0L) {
      return
    }
    require(bytes >= 0) { "bytes must be greater than or equal to zero." }
    require(buffer.capacity > 0) { "Buffer capacity must be greater than or equal to zero." }
    var skipRemaining = bytes
    while (skipRemaining > 0) {
      val forRead = minOf(buffer.capacity, skipRemaining.toInt())
      buffer.position = 0
      buffer.limit = forRead
      readFully(buffer)
      skipRemaining -= forRead
    }
  }

  /**
   * Returns Input with size [limit]
   */
  fun withLimit(limit: Long): Input = when {
    limit == 0L -> NullInput
    else -> InputWithLimit(limit = limit, source = this)
  }
}

@Throws(EOFException::class)
fun Input.readByteArray(size: Int, bufferProvider: ByteBufferProvider): ByteArray {
  require(size >= 0) { "size should be more or equals 0" }
  if (size == 0) {
    return byteArrayOf()
  }
  val array = ByteArray(size)
  readByteArray(
    dest = array,
    bufferProvider = bufferProvider,
  )
  return array
}

@Throws(EOFException::class)
fun Input.readByteArray(dest: ByteArray, bufferProvider: ByteBufferProvider) {
  if (dest.isEmpty()) {
    return
  }
  bufferProvider.using { buffer ->
    var cursor = 0
    while (cursor < dest.size) {
      buffer.reset(0, minOf(dest.size - cursor, buffer.capacity))
      val len = read(buffer)
      if (len.isNotAvailable) {
        throw EOFException("Read $cursor/${dest.size}, can't read ${dest.size - cursor}")
      }
      buffer.flip()
      buffer.readInto(dest, offset = cursor)
      cursor += len.length
    }
  }
}

private class InputWithLimit(val limit: Long, val source: Input) : Input {
  init {
    require(limit >= 0) { "limit must be greater than or equal to zero." }
  }

  var remaining: Long = limit
    private set

  override fun read(dest: ByteBuffer): DataTransferSize {
    val rem = dest.remaining
    if (remaining <= 0) {
      return DataTransferSize.EMPTY
    }
    if (rem <= 0) {
      return DataTransferSize.EMPTY
    }
    if (rem > remaining) {
      dest.limit = dest.position + remaining.toInt()
    }
    val wasRead = source.read(dest)
    if (wasRead.isAvailable) {
      remaining -= wasRead.length
    }
    return wasRead
  }

  override fun close() {
    remaining = 0
    source.close()
  }

}

object NullInput : Input {
  override fun read(dest: ByteBuffer): DataTransferSize = DataTransferSize.EMPTY

  override fun close() {
    // Do nothing
  }
}
