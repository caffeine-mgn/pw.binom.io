package pw.binom

import pw.binom.io.ByteBuffer
import pw.binom.io.Output
import pw.binom.io.UTF8
import pw.binom.uuid.UUID

fun <T : Output> T.writeUtf8Char(buffer: ByteBuffer, value: Char): T {
  buffer.clear()
  val size = UTF8.unicodeToUtf8(value, buffer)
  buffer.reset(0, size)
  write(buffer)
  return this
}

fun <T : Output> T.writeUTF8String(buffer: ByteBuffer, text: String): T {
  writeInt(buffer, text.length)
  text.forEach {
    writeUtf8Char(buffer, it)
  }
  return this
}

fun <T : Output> T.writeByte(buffer: ByteBuffer, value: Byte): T {
  buffer[0] = value
  buffer.reset(0, 1)
  write(buffer)
  return this
}

fun <T : Output> T.writeBytes(pool: ByteBufferPool, value: ByteArray): T {
  val buf = pool.borrow()
  try {
    writeBytes(buf, value)
  } finally {
    pool.recycle(buf)
  }
  return this
}

fun <T : Output> T.writeBytes(buffer: ByteBuffer, value: ByteArray): T {
  buffer.clear()
  var l = value.size
  while (l > 0) {
    buffer.write(
      data = value,
      offset = value.size - l,
      length = minOf(value.size - (value.size - l), buffer.remaining),
    )
    buffer.flip()
    val wrote = write(buffer)
    if (wrote.isNotAvailable) {
      throw RuntimeException("Can't write bytes")
    }
    l -= wrote.length
  }
  return this
}

fun <T : Output> T.writeUUID(buffer: ByteBuffer, value: UUID): T {
  writeLong(buffer, value.mostSigBits)
  writeLong(buffer, value.leastSigBits)
  return this
}

fun <T : Output> T.writeInt(buffer: ByteBuffer, value: Int): T {
  buffer.clear()
  value.toByteBuffer(buffer)
  buffer.flip()
  write(buffer)
  return this
}

fun <T : Output> T.writeShort(buffer: ByteBuffer, value: Short): T {
  buffer.clear()
  value.toByteBuffer(buffer)
  buffer.flip()
  write(buffer)
  return this
}

fun <T : Output> T.writeLong(buffer: ByteBuffer, value: Long): T {
  buffer.clear()
  value.toByteBuffer(buffer)
  buffer.flip()
  write(buffer)
  return this
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Output> T.writeFloat(buffer: ByteBuffer, value: Float) = writeInt(buffer, value.toBits())

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Output> T.writeDouble(buffer: ByteBuffer, value: Double) = writeLong(buffer, value.toBits())
