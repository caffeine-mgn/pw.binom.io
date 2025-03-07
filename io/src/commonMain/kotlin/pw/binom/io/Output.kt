package pw.binom.io

import pw.binom.toByteArray

interface Output : Closeable {
  companion object {
    val NULL = NullOutput
  }

  fun write(data: ByteBuffer): DataTransferSize
  fun write(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): DataTransferSize =
    data.wrap {
      it.position = offset
      it.limit = offset + length
      write(it)
    }

  fun flush()
  fun writeFully(data: ByteBuffer) {
    while (data.remaining > 0) {
      val wrote = write(data)
      if (wrote.isNotAvailable) {
        throw IOException("Can't write data")
      }
    }
  }

  fun writeFully(data: ByteArray, offset: Int = 0, length: Int = data.size - offset) {
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

  fun writeByte(value: Byte) {
    writeFully(ByteArray(1) { value })
  }

  fun writeShort(value: Short) {
    writeFully(value.toByteArray())
  }

  fun writeInt(value: Int) {
    writeFully(value.toByteArray())
  }

  fun writeLong(value: Long) {
    writeFully(value.toByteArray())
  }

  fun writeFloat(value: Float) {
    writeInt(value.toBits())
  }

  fun writeDouble(value: Double) {
    writeLong(value.toBits())
  }

  fun writeString(value: String) {
    val data = value.encodeToByteArray()
    writeInt(data.size)
    writeFully(data)
  }
}

object NullOutput : Output {
  override fun write(data: ByteBuffer): DataTransferSize {
    val remaining = data.remaining
    data.empty()
    return DataTransferSize.ofSize(remaining)
  }

  override fun writeFully(data: ByteBuffer) {
    data.position = data.limit
  }

  override fun write(data: ByteArray, offset: Int, length: Int): DataTransferSize =
    DataTransferSize.ofSize(length)

  override fun close() {
    // Do nothing
  }

  override fun flush() {
    // Do nothing
  }
}

fun Output.writeByteArray(data: ByteArray, bufferProvider: ByteBufferProvider) {
  if (data.isEmpty()) {
    return
  }
  bufferProvider.using { buffer ->
    require(buffer.capacity > 0) { "Buffer capacity should be more than 0" }
    var cursor = 0
    while (cursor < data.size) {
      buffer.clear()
      val len = buffer.write(data, offset = cursor)
      if (len.isNotAvailable) {
        break
      }
      buffer.flip()
      writeFully(buffer)
      cursor += len.length
    }
  }
}
