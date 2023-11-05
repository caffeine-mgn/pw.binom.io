package pw.binom

import pw.binom.io.*
import pw.binom.pool.using
import pw.binom.uuid.UUID

fun Input.asyncInput(callClose: Boolean = true) = object : AsyncInput {
  override val available: Int
    get() = -1

  override suspend fun read(dest: ByteBuffer): Int =
    this@asyncInput.read(dest)

  override suspend fun asyncClose() {
    if (callClose) {
      this@asyncInput.close()
    }
  }

  override suspend fun skipAll(bufferSize: Int) {
    this@asyncInput.skipAll(bufferSize)
  }

  override suspend fun skipAll(buffer: ByteBuffer) {
    this@asyncInput.skipAll(buffer)
  }

  override suspend fun skip(bytes: Long, bufferSize: Int) {
    this@asyncInput.skip(bytes, bufferSize)
  }

  override suspend fun skip(bytes: Long, buffer: ByteBuffer) {
    this@asyncInput.skip(bytes, buffer)
  }

  override suspend fun readFully(dest: ByteBuffer): Int {
    return this@asyncInput.readFully(dest)
  }
}

suspend fun AsyncInput.readUtf8Char(pool: ByteBufferProvider): Char? {
  pool.using { buffer ->
    buffer.reset(0, 1)
    return if (read(buffer) == 0) {
      null
    } else {
      val firstByte = buffer[0]
      val size = UTF8.getUtf8CharSize(firstByte) - 1
      if (size > 0) {
        buffer.reset(0, size)
        read(buffer)
      }
      UTF8.utf8toUnicode(firstByte, buffer)
    }
  }
}

suspend fun AsyncInput.readUTF8String(pool: ByteBufferProvider): String {
  pool.using { buffer ->
    val size = readInt(buffer)
    val sb = StringBuilder(size)
    repeat(size) {
      sb.append(readUtf8Char(buffer) ?: throw EOFException())
    }
    return sb.toString()
  }
}

suspend fun AsyncInput.readByte(pool: ByteBufferPool): Byte {
  pool.using { buffer ->
    buffer.reset(0, 1)
    readFully(buffer)
    buffer.flip()
    return buffer[0]
  }
}

suspend fun AsyncInput.readByte(buffer: ByteBuffer): Byte {
  buffer.reset(0, 1)
  readFully(buffer)
  buffer.flip()
  return buffer[0]
}

suspend fun AsyncInput.readUUID(pool: ByteBufferPool) = UUID.create(
  mostSigBits = readLong(pool),
  leastSigBits = readLong(pool),
)

suspend fun AsyncInput.readUUID(buffer: ByteBuffer) = UUID.create(
  mostSigBits = readLong(buffer),
  leastSigBits = readLong(buffer),
)

suspend fun AsyncInput.readInt(pool: ByteBufferPool): Int {
  pool.using { buffer ->
    buffer.reset(0, 4)
    readFully(buffer)
    buffer.flip()
    return Int.fromBytes(buffer[0], buffer[1], buffer[2], buffer[3])
  }
}

suspend fun AsyncInput.readInt(buffer: ByteBuffer): Int {
  buffer.reset(0, 4)
  readFully(buffer)
  buffer.flip()
  return Int.fromBytes(buffer[0], buffer[1], buffer[2], buffer[3])
}

suspend fun AsyncInput.readShort(buffer: ByteBuffer): Short {
  buffer.reset(0, 2)
  readFully(buffer)
  buffer.flip()
  return Short.fromBytes(buffer[0], buffer[1])
}

suspend fun AsyncInput.readShort(pool: ByteBufferPool): Short = pool.using { buffer ->
  readShort(buffer = buffer)
}

suspend fun AsyncInput.readLong(buffer: ByteBuffer): Long {
  buffer.reset(position = 0, length = Long.SIZE_BYTES)
  readFully(buffer)
  buffer.flip()

  return Long.fromBytes(
    buffer.getByte(),
    buffer.getByte(),
    buffer.getByte(),
    buffer.getByte(),
    buffer.getByte(),
    buffer.getByte(),
    buffer.getByte(),
    buffer.getByte(),
  )
}

suspend fun AsyncInput.readLong(pool: ByteBufferPool): Long = pool.using { buffer ->
  buffer.reset(position = 0, length = Long.SIZE_BYTES)
  readFully(buffer)
  buffer.flip()

  Long.fromBytes(
    buffer.getByte(),
    buffer.getByte(),
    buffer.getByte(),
    buffer.getByte(),
    buffer.getByte(),
    buffer.getByte(),
    buffer.getByte(),
    buffer.getByte(),
  )
}

suspend inline fun AsyncInput.readFloat(buffer: ByteBuffer) = Float.fromBits(readInt(buffer))
suspend inline fun AsyncInput.readFloat(pool: ByteBufferPool) = pool.using { buffer ->
  readFloat(buffer)
}

suspend inline fun AsyncInput.readDouble(buffer: ByteBuffer) = Double.fromBits(readLong(buffer))
suspend inline fun AsyncInput.readDouble(pool: ByteBufferPool) = pool.using { buffer ->
  readDouble(buffer)
}

/**
 * Copy date from [this] to [dest]
 *
 * @receiver input
 * @param dest output
 * @param buffer buffer for coping data from [this] to [dest]
 * @return size of copied data
 */
suspend fun AsyncInput.copyTo(dest: AsyncOutput, buffer: ByteBuffer): Long {
  var totalLength = 0L
  while (true) {
    buffer.clear()
    val length = read(buffer)
    if (length == 0) {
      break
    }
    totalLength += length.toLong()
    buffer.flip()
    dest.writeFully(buffer)
  }
  return totalLength
}

suspend fun AsyncInput.copyTo(dest: AsyncOutput, pool: ByteBufferPool): Long = pool.using { buffer ->
  copyTo(dest = dest, buffer = buffer)
}

suspend fun AsyncInput.copyTo(dest: Output, buffer: ByteBuffer): Long {
  var totalLength = 0L
  while (true) {
    buffer.clear()
    val length = read(buffer)
    if (length == 0) {
      break
    }
    totalLength += length.toLong()
    buffer.flip()
    dest.write(buffer)
  }
  return totalLength
}

// /**
// * Copy date from [this] to [output]
// *
// * @receiver input
// * @param output output
// * @param pool for get temp coping buffer
// * @return size of copied data
// */
// suspend fun AsyncInput.copyTo(output: AsyncOutput, pool: ObjectPool<ByteBuffer>): Long {
//    val buffer = pool.borrow()
//    try {
//        return copyTo(output, buffer)
//    } finally {
//        pool.recycle(buffer)
//    }
// }

/**
 * Copy date from [this] to [dest]
 *
 * @receiver input
 * @param dest output
 * @param bufferSize coping buffer size
 * @return size of copied data
 */
suspend fun AsyncInput.copyTo(dest: AsyncOutput, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long =
  ByteBuffer(bufferSize).use { buffer ->
    copyTo(dest = dest, buffer = buffer)
  }

/**
 * Copy date from [this] to [output]
 *
 * @receiver input
 * @param output output
 * @return size of copied data
 */
suspend fun AsyncInput.copyTo(output: Output, pool: ByteBufferPool): Long = pool.using { buffer ->
  copyTo(dest = output, buffer = buffer)
}

suspend fun AsyncInput.copyTo(output: Output, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long =
  ByteBuffer(bufferSize).use { buffer ->
    copyTo(dest = output, buffer = buffer)
  }

object EmptyAsyncInput : AsyncInput {
  override val available: Int
    get() = 0

  override suspend fun read(dest: ByteBuffer): Int = 0

  override suspend fun asyncClose() {
    // Do nothing
  }
}

suspend fun AsyncInput.readByteArray(size: Int, pool: ByteBufferPool): ByteArray = pool.using { buffer ->
  readByteArray(size = size, buffer = buffer)
}

suspend fun AsyncInput.readByteArray(size: Int, buffer: ByteBuffer): ByteArray {
  require(size >= 0) { "size should be more or equals 0" }
  val array = ByteArray(size)
  readByteArray(
    dest = array,
    buffer = buffer,
  )
  return array
}

suspend fun AsyncInput.readByteArray(dest: ByteArray, pool: ByteBufferPool) {
  pool.using { buffer ->
    readByteArray(dest = dest, buffer = buffer)
  }
}

suspend fun AsyncInput.readByteArray(dest: ByteArray, buffer: ByteBuffer) {
  if (dest.isEmpty()) {
    return
  }
  var cursor = 0
  while (cursor < dest.size) {
    buffer.reset(0, minOf(dest.size - cursor, buffer.capacity))
    val len = read(buffer)
    if (len == 0) {
      throw EOFException("Read $cursor/${dest.size}, can't read ${dest.size - cursor}")
    }
    buffer.flip()
    val cp = buffer.readInto(dest = dest, offset = cursor)
    cursor += len
  }
}
