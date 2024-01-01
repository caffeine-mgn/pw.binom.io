package pw.binom.io

import pw.binom.io.AsyncOutput.Companion.NullAsyncOutput.write
import pw.binom.pool.ObjectPool
import pw.binom.pool.using

interface AsyncOutput : AsyncCloseable, AsyncFlushable {
  companion object {
    /**
     * Special AsyncOutput for drop all output passed to [write]
     */
    private object NullAsyncOutput : AsyncOutput {
      override suspend fun write(data: ByteBuffer): Int {
        val remaining = data.remaining
        data.empty()
        return remaining
      }

      override suspend fun asyncClose() {
        // Do nothing
      }

      override suspend fun flush() {
        // Do nothing
      }
    }

    val NULL: AsyncOutput = NullAsyncOutput
  }

  //    suspend fun write(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int
  suspend fun write(data: ByteBuffer): Int

  suspend fun writeFully(data: ByteBuffer): Int {
    var writeSize = 0
    while (data.remaining > 0) {
      val wrote = write(data)
      if (wrote <= 0) {
        throw IOException("Can't write data")
      }
      writeSize += wrote
    }
    return writeSize
  }
}

fun AsyncOutput.withCounter() = AsyncOutputWithWriteCounter(this)

class AsyncOutputWithWriteCounter(val stream: AsyncOutput) : AsyncOutput {
  var writedBytes = 0L

  override suspend fun write(data: ByteBuffer): Int {
    val r = stream.write(data)
    writedBytes += r
    return r
  }

  override suspend fun asyncClose() {
    stream.flush()
  }

  override suspend fun flush() {
    stream.flush()
  }
}

suspend fun AsyncOutput.writeByteArray(value: ByteArray) {
  value.wrap {
    writeFully(it)
  }
}

suspend fun AsyncOutput.writeByteArray(value: ByteArray, buffer: ByteBuffer) {
  require(buffer.capacity > 0) { "Buffer capacity should be more than 0" }
  var cursor = 0
  while (cursor < value.size) {
    buffer.clear()
    val len = buffer.write(value, offset = cursor)
    if (len <= 0) {
      break
    }
    buffer.flip()
    writeFully(buffer)
    cursor += len
  }
}

suspend fun AsyncOutput.writeByteArray(value: ByteArray, pool: ObjectPool<PooledByteBuffer>) {
  pool.using { buffer ->
    writeByteArray(value = value, buffer = buffer)
  }
}

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

  override suspend fun write(data: ByteBuffer): Int {
    val len = data.remaining
    sb.ensureCapacity(sb.length + len)
    data.forEach { byte ->
      sb.append(byte.toInt().toChar())
    }
    data.position = data.limit
    return len
  }

  override suspend fun asyncClose() {
  }

  override suspend fun flush() {
  }

  override fun toString(): String = sb.toString()
}
