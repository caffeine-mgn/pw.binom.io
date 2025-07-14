package pw.binom.io

import pw.binom.pool.ObjectPool
import pw.binom.pool.using
import pw.binom.toByteArray

interface AsyncOutput : AsyncCloseable, AsyncFlushable {
  companion object {
    /**
     * Special AsyncOutput for drop all output passed to [write]
     */
    private object NullAsyncOutput : AsyncOutput {
      override suspend fun write(data: ByteBuffer): DataTransferSize {
        val remaining = data.remaining
        data.empty()
        return DataTransferSize.ofSize(remaining)
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
  suspend fun write(data: ByteBuffer): DataTransferSize
  suspend fun write(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): DataTransferSize =
    data.wrap {
      it.position = offset
      it.limit = offset + length
      write(it)
    }

  suspend fun writeFully(data: ByteArray, offset: Int = 0, length: Int = data.size - offset) {
    var cursor = offset
    var wrote = 0
    fun remaining() = length - wrote
    while (remaining() > 0) {
      val len = write(data = data, offset = cursor, length = remaining())
      if (len.isNotAvailable) {
        throw if (wrote > 0) {
          PackageBreakException("Wrote $wrote bytes")
        } else {
          StreamClosedException()
        }
      }
      val l = len.length
      cursor += l
      wrote += l
    }
  }

  suspend fun writeFully(data: ByteBuffer): Int {
    var writeSize = 0
    while (data.remaining > 0) {
      val wrote = write(data)
      if (wrote.isNotAvailable) {
        if (writeSize != 0) {
          throw PackageBreakException("Can't write data. $writeSize bytes was sent")
        }
        if (wrote.isEof) {
          throw IllegalStateException("Stream is empty")
        }
        throw StreamClosedException()
      }
      writeSize += wrote.length
    }
    return writeSize
  }

  suspend fun writeByte(value: Byte) {
    writeFully(ByteArray(1) { value })
  }

  suspend fun writeShort(value: Short) {
    writeFully(value.toByteArray())
  }

  suspend fun writeInt(value: Int) {
    writeFully(value.toByteArray())
  }

  suspend fun writeLong(value: Long) {
    writeFully(value.toByteArray())
  }

  suspend fun writeFloat(value: Float) {
    writeInt(value.toBits())
  }

  suspend fun writeDouble(value: Double) {
    writeLong(value.toBits())
  }

  suspend fun writeString(value: String) {
    val data = value.encodeToByteArray()
    writeInt(data.size)
    writeFully(data)
  }
}

fun AsyncOutput.withCounter() = AsyncOutputWithWriteCounter(this)

class AsyncOutputWithWriteCounter(val stream: AsyncOutput) : AsyncOutput {
  var writedBytes = 0L

  override suspend fun write(data: ByteBuffer): DataTransferSize {
    val r = stream.write(data)
    if (r.isAvailable) {
      writedBytes += r.length
    }
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
    if (len.isNotAvailable) {
      break
    }
    buffer.flip()
    writeFully(buffer)
    cursor += len.length
  }
}

suspend fun AsyncOutput.writeByteArray(value: ByteArray, pool: ObjectPool<PooledByteBuffer>) {
  pool.using { buffer ->
    writeByteArray(value = value, buffer = buffer)
  }
}
