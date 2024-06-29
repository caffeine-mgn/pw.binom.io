package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE

interface Input : Closeable {
  fun read(dest: ByteBuffer): Int
  fun readFully(dest: ByteBuffer): Int {
    val length = dest.remaining
    while (dest.remaining > 0 && dest.remaining > 0) {
      if (read(dest) == 0) {
        throw EOFException()
      }
    }
    return length
  }

  fun skipAll(bufferSize: Int = DEFAULT_BUFFER_SIZE) {
    ByteBuffer(bufferSize).use { buffer ->
      skipAll(buffer = buffer)
    }
  }

  fun skipAll(buffer: ByteBuffer) {
    while (true) {
      buffer.clear()
      if (read(buffer) == 0) {
        break
      }
    }
  }

  @Throws(EOFException::class)
  fun skip(bytes: Long, bufferSize: Int = DEFAULT_BUFFER_SIZE) {
    ByteBuffer(bufferSize).use { buffer ->
      skip(bytes = bytes, buffer = buffer)
    }
  }

  @Throws(EOFException::class)
  fun skip(bytes: Long, buffer: ByteBuffer) {
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
  fun withLimit(limit: Long): Input = InputWithLimit(limit = limit, source = this)
}

@Throws(EOFException::class)
fun Input.readByteArray(size: Int, bufferProvider: ByteBufferProvider): ByteArray {
  require(size >= 0) { "size should be more or equals 0" }
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
      if (len == 0) {
        throw EOFException("Read $cursor/${dest.size}, can't read ${dest.size - cursor}")
      }
      buffer.flip()
      buffer.readInto(dest, offset = cursor)
      cursor += len
    }
  }
}

private class InputWithLimit(val limit: Long, val source: Input) : Input {
  var remaining: Long = limit
    private set

  override fun read(dest: ByteBuffer): Int {
    val rem = dest.remaining
    if (remaining <= 0) {
      return 0
    }
    if (rem <= 0) {
      return 0
    }
    if (rem > remaining) {
      dest.limit = dest.position + remaining.toInt()
    }
    val wasRead = source.read(dest)
    if (wasRead > 0) {
      remaining -= wasRead
    }
    return wasRead
  }

  override fun close() {
    remaining = 0
    source.close()
  }

}

object NullInput : Input {
  override fun read(dest: ByteBuffer): Int = 0

  override fun close() {
    // Do nothing
  }
}
