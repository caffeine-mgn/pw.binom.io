package pw.binom.memory

import kotlinx.cinterop.*
import platform.memory.common.*

@OptIn(ExperimentalForeignApi::class)
actual class ForeignMemory(val raw: CPointer<ByteVar>, size: ULong) : AutoCloseable {
  actual companion object {
    actual fun allocate(length: ULong): ForeignMemory =
      ForeignMemory(
        raw = internal_alloc(length.toLong())!!,
        size = length
      )
  }

  actual val size: ULong = size

  actual fun fill(value: Byte, offset: ULong, size: ULong) {
    internal_memset(raw + offset.toLong(), size.toLong(), value)
  }

  actual fun copyFrom(
    src: ForeignMemory,
    srcOffset: ULong,
    offset: ULong,
    size: ULong,
  ) {
    internal_copy(
      src = src.raw + srcOffset.toLong(),
      dest = raw + offset.toLong(),
      size = size.toLong(),
    )
  }

  actual fun getByte(offset: ULong): Byte =
    raw[offset.toLong()]

  actual fun setByte(offset: ULong, value: Byte) {
    raw[offset.toLong()] = value
  }

  actual fun getShort(offset: ULong): Short =
    internal_getShort(raw + offset.toLong())

  actual fun setShort(offset: ULong, value: Short) {
    internal_setShort(raw + offset.toLong(), value)
  }

  actual fun getInt(offset: ULong): Int =
    internal_getInt(raw + offset.toLong())

  actual fun setInt(offset: ULong, value: Int) {
    internal_setInt(raw + offset.toLong(), value)
  }

  actual fun getLong(offset: ULong): Long =
    internal_getLong(raw + offset.toLong())

  actual fun setLong(offset: ULong, value: Long) {
    internal_setLong(raw + offset.toLong(), value)
  }

  actual fun setString(offset: ULong, value: String) {
    value.encodeToByteArray().usePinned {
      internal_copy(src = it.addressOf(0), dest = raw, size = it.get().size.toLong())
      raw[it.get().size.toLong() + 1] = 0
    }
  }

  actual fun getString(offset: ULong): String {
    val len = internal_strlen(raw + offset.toLong())
    if (len <= -1) {
      TODO()
    }
    if (len.toULong() + offset > size) {
      TODO()
    }
    return (raw + offset.toLong())!!.toKStringFromUtf8()
  }

  actual override fun close() {
    internal_free(raw)
  }

  actual fun copyFrom(src: ByteArray, srcOffset: ULong, offset: ULong, size: ULong) {
    src.usePinned {
      internal_copy(it.addressOf(0) + srcOffset.toLong(), raw + offset.toLong(), size.toLong())
    }
  }
  actual fun copyTo(dest: ByteArray, destOffset: ULong, offset: ULong, size: ULong) {
    dest.usePinned {
      internal_copy(raw + offset.toLong(),it.addressOf(0) + destOffset.toLong(), size.toLong())
    }
  }
}
