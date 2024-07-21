package pw.binom.io

import pw.binom.fromBytes

class ByteArrayInput(val data: ByteArray) : Input {
  private var closed = false
  private var cursor = 0

  override fun read(dest: ByteBuffer): DataTransferSize {
    checkClosed()
    val max = minOf(data.size - cursor, dest.remaining)
    if (max == 0) {
      return DataTransferSize.EMPTY
    }
    dest.write(
      data = data,
      offset = cursor,
      length = max,
    )
    cursor += max
    return DataTransferSize.ofSize(max)
  }

  private fun checkClosed() {
    if (closed) {
      throw ClosedException()
    }
  }

  override fun close() {
    checkClosed()
    closed = true
  }

  fun readByte(): Byte {
    if (cursor >= data.size) {
      throw EOFException()
    }
    val result = data[cursor]
    cursor++
    return result
  }

  fun readInt(): Int {
    if (cursor + Int.SIZE_BYTES - 1 >= data.size) {
      throw EOFException()
    }
    val result = Int.fromBytes(data, offset = cursor)
    cursor += Int.SIZE_BYTES
    return result
  }

  fun readLong(): Long {
    if (cursor + Long.SIZE_BYTES - 1 >= data.size) {
      throw EOFException()
    }
    val result = Long.fromBytes(data, offset = cursor)
    cursor += Long.SIZE_BYTES
    return result
  }

  fun readShort(): Short {
    if (cursor + Short.SIZE_BYTES - 1 >= data.size) {
      throw EOFException()
    }
    val result = Short.fromBytes(data, offset = cursor)
    cursor += Short.SIZE_BYTES
    return result
  }

  fun readBytes(
    destination: ByteArray,
    offset: Int = 0,
    length: Int = destination.size - offset,
  ) {
    require(offset >= 0)
    if (length == 0) {
      return
    }
    require(length > 0)
    require(destination.size - offset >= length)
    require(data.size - cursor >= length) { "No data. Need $length, but actual has ${data.size - cursor}" }
    data.copyInto(
      destination = destination,
      destinationOffset = offset,
      startIndex = cursor,
      endIndex = cursor + length,
    )
    cursor += length
  }

  fun readBytes(size: Int): ByteArray {
    val out = ByteArray(size)
    readBytes(destination = out)
    return out
  }

  fun readFloat() = Float.fromBits(readInt())

  fun readDouble() = Double.fromBits(readLong())
}
