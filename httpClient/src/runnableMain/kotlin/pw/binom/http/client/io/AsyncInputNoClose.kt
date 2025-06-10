package pw.binom.http.client.io

import pw.binom.io.*
import pw.binom.io.http.AsyncHttpInput

class AsyncInputNoClose(val source: AsyncHttpInput) : AsyncHttpInput {

  private var closed = false
  fun markClosed() {
    closed = true
  }

  override fun toString(): String = "AsyncInputNoClose($source)"

  override val available: Available
    get() = if (closed) {
      Available.NOT_AVAILABLE
    } else {
      source.available
    }
  override val isEof: Boolean
    get() = source.isEof

  override suspend fun asyncClose() {
  }

  override suspend fun read(dest: ByteBuffer): DataTransferSize {
    if (closed) {
      return DataTransferSize.CLOSED
    }
    return source.read(dest)
  }

  override suspend fun skipAll(buffer: ByteBuffer) {
    if (closed) {
      throw ClosedException()
    }
    source.skipAll(buffer)
  }

  override suspend fun skipAll(bufferSize: Int) {
    if (closed) {
      throw ClosedException()
    }
    source.skipAll(bufferSize)
  }

  override suspend fun skip(bytes: Long, buffer: ByteBuffer) {
    if (closed) {
      throw ClosedException()
    }
    source.skip(bytes, buffer)
  }

  override suspend fun skip(bytes: Long, bufferSize: Int) {
    if (closed) {
      throw ClosedException()
    }
    source.skip(bytes, bufferSize)
  }

  override suspend fun readString(): String {
    if (closed) {
      throw ClosedException()
    }
    return source.readString()
  }

  override suspend fun readShort(): Short {
    if (closed) {
      throw ClosedException()
    }
    return source.readShort()
  }

  override suspend fun readLong(): Long {
    if (closed) {
      throw ClosedException()
    }
    return source.readLong()
  }

  override suspend fun readInt(): Int {
    if (closed) {
      throw ClosedException()
    }
    return source.readInt()
  }

  override suspend fun readFully(dest: ByteBuffer): Int {
    if (closed) {
      return 0
    }
    return source.readFully(dest)
  }

  override suspend fun readFully(dest: ByteArray, offset: Int, length: Int) {
    if (closed) {
      throw ClosedException()
    }
    source.readFully(dest, offset, length)
  }

  override suspend fun readFloat(): Float {
    if (closed) {
      throw ClosedException()
    }
    return source.readFloat()
  }

  override suspend fun readDouble(): Double {
    if (closed) {
      throw ClosedException()
    }
    return source.readDouble()
  }

  override suspend fun readByte(): Byte {
    if (closed) {
      throw ClosedException()
    }
    return source.readByte()
  }

  override suspend fun readBoolean(): Boolean {
    if (closed) {
      throw ClosedException()
    }
    return source.readBoolean()
  }

  override suspend fun read(dest: ByteArray, offset: Int, length: Int): DataTransferSize {
    if (closed) {
      return DataTransferSize.CLOSED
    }
    return source.read(dest, offset, length)
  }
}
