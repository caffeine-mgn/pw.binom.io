package pw.binom.memory

actual class ForeignMemory(val native: ByteArray) : AutoCloseable {

  actual override fun close() {
    // Do nothing
  }

  actual companion object {
    actual fun allocate(length: ULong): ForeignMemory = ForeignMemory(ByteArray(length.toInt()))
  }

  actual val size: ULong
    get() = native.size.toULong()

  actual fun fill(value: Byte, offset: ULong, size: ULong) {
    for (i in offset.toInt() until (offset + size).toInt()) {
      native[i] = value
    }
  }

  actual fun copyFrom(
    src: ForeignMemory,
    srcOffset: ULong,
    offset: ULong,
    size: ULong,
  ) {
    src.native.copyInto(
      destination = native,
      destinationOffset = offset.toInt(),
      startIndex = srcOffset.toInt(),
      endIndex = (srcOffset + size).toInt()
    )
  }

  actual fun copyFrom(src: ByteArray, srcOffset: ULong, offset: ULong, size: ULong) {
    src.copyInto(
      destination = native,
      destinationOffset = offset.toInt(),
      startIndex = srcOffset.toInt(),
      endIndex = srcOffset.toInt() + size.toInt()
    )
  }

  actual fun copyTo(dest: ByteArray, destOffset: ULong, offset: ULong, size: ULong) {
    native.copyInto(
      destination = dest,
      destinationOffset = destOffset.toInt(),
      startIndex = offset.toInt(),
      endIndex = offset.toInt() + size.toInt()
    )
  }

  actual fun getByte(offset: ULong): Byte = native[offset.toInt()]

  actual fun setByte(offset: ULong, value: Byte) {
    native[offset.toInt()] = value
  }

  actual fun getShort(offset: ULong): Short {
    TODO("Not yet implemented")
  }

  actual fun setShort(offset: ULong, value: Short) {
  }

  actual fun getInt(offset: ULong): Int {

    TODO("Not yet implemented")
  }

  actual fun setInt(offset: ULong, value: Int) {
  }

  actual fun getLong(offset: ULong): Long {
    TODO("Not yet implemented")
  }

  actual fun setLong(offset: ULong, value: Long) {

  }

  actual fun setString(offset: ULong, value: String) {
    value.encodeToByteArray().copyInto(
      destination = native,
      destinationOffset = offset.toInt(),
    )
  }

  actual fun getString(offset: ULong): String {
    for (i in offset.toInt() until native.size) {
      if (native[i] == 0.toByte()) {
        return native.decodeToString(startIndex = offset.toInt(), endIndex = i - 1)
      }
    }
    TODO("Not yet implemented")
  }
}
