package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.fromBytes
import kotlin.coroutines.cancellation.CancellationException

interface AsyncInput : AsyncCloseable {
  companion object {
    private object EmptyAsyncInput : AsyncInput {
      override val available: Available
        get() = Available.NOT_AVAILABLE

      override suspend fun read(dest: ByteBuffer) = DataTransferSize.EMPTY

      override suspend fun asyncClose() {
        // Do nothing
      }
    }

    val EMPTY: AsyncInput = EmptyAsyncInput
  }

  /**
   * Available Data size in bytes
   * @return Available data in bytes. If returns value less 0 it's mean that size of available data is unknown
   */
  val available: Available

  suspend fun skipAll(bufferSize: Int = DEFAULT_BUFFER_SIZE) {
    ByteBuffer(bufferSize).use { buffer ->
      skipAll(buffer = buffer)
    }
  }

  suspend fun skipAll(buffer: ByteBuffer) {
    while (true) {
      buffer.clear()
      if (read(buffer).isNotAvailable) {
        break
      }
    }
  }

  @Throws(EOFException::class, CancellationException::class)
  suspend fun skip(bytes: Long, bufferSize: Int = DEFAULT_BUFFER_SIZE) {
    ByteBuffer(bufferSize).use { buffer ->
      skip(bytes = bytes, buffer = buffer)
    }
  }

  @Throws(EOFException::class, CancellationException::class)
  suspend fun skip(bytes: Long, buffer: ByteBuffer) {
    var skipRemaining = bytes
    while (skipRemaining > 0) {
      val forRead = minOf(buffer.capacity, skipRemaining.toInt())
      buffer.position = 0
      buffer.limit = forRead
      readFully(buffer)
      skipRemaining -= forRead
    }
  }

  suspend fun read(dest: ByteArray, offset: Int = 0, length: Int = dest.size - offset): DataTransferSize =
    dest.wrap {
      it.position = offset
      it.limit = offset + length
      read(it)
    }

  suspend fun readFully(dest: ByteArray, offset: Int = 0, length: Int = dest.size - offset) {
    var wasRead = 0
    while (true) {
      val r = read(dest, offset = offset + wasRead, length = length - wasRead)
      if (r.isAvailable) {
        wasRead += r.length
        if (wasRead == length) {
          return
        } else {
          continue
        }
      }
      if (wasRead > 0) {
        throw PackageBreakException()
      } else {
        throw EOFException()
      }
    }

//    var cursor = offset
//    var wasRead = 0
//    fun remaining() = length - wasRead
//    while (remaining() > 0) {
//      val len = read(dest = dest, offset = cursor, length = remaining())
//      if (len.isNotAvailable) {
//        throw if (wasRead > 0) {
//          PackageBreakException("Wrote $wasRead bytes")
//        } else {
//          StreamClosedException()
//        }
//      }
//      val l = len.length
//      cursor += l
//      wasRead += l
//    }
  }

  suspend fun read(dest: ByteBuffer): DataTransferSize
  suspend fun readFully(dest: ByteBuffer): Int {
    val length = dest.remaining
    var wasRead = 0
    while (dest.remaining > 0) {
      val read = read(dest)
      if (read.isNotAvailable && dest.remaining > 0) {
        val msg = "Full message $length bytes, can't read ${dest.remaining} bytes"
        if (wasRead > 0) {
          throw PackageBreakException("$msg. Was read $wasRead bytes")
        } else {
          throw EOFException(msg)
        }
      }
      wasRead += read.length
    }
    return length
  }

  fun withLimit(limit: Long, closeParent: Boolean = true): AsyncInput =
    AsyncInputWithLimit(
      limit = limit,
      source = this,
      closeParent = closeParent,
    )

  suspend fun readBoolean() = readByte() > 0

  suspend fun readInt(): Int {
    val buf = ByteArray(Int.SIZE_BYTES)
    readFully(buf)
    return Int.fromBytes(buf)
  }

  suspend fun readShort(): Short {
    val buf = ByteArray(Short.SIZE_BYTES)
    readFully(buf)
    return Short.fromBytes(buf)
  }

  suspend fun readLong(): Long {
    val buf = ByteArray(Long.SIZE_BYTES)
    readFully(buf)
    return Long.fromBytes(buf)
  }

  suspend fun readFloat() = Float.fromBits(readInt())
  suspend fun readDouble() = Double.fromBits(readLong())

  suspend fun readString(): String {
    val size = readInt()
    val bytes = ByteArray(size)
    readFully(bytes)
    return bytes.decodeToString()
  }

  suspend fun readByte(): Byte {
    val r = ByteArray(1)
    readFully(r)
    return r[0]
  }
}
