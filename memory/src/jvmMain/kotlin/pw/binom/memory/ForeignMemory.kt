package pw.binom.memory

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

actual class ForeignMemory(val native: MemorySegment) : AutoCloseable {
  actual override fun close() {
    if (native.isMapped) {
      native.unload()
    }
  }

  actual companion object {
    actual fun allocate(length: ULong): ForeignMemory =
      ForeignMemory(Arena.global().allocate(length.toLong()))
  }

  actual val size: ULong
    get() = native.byteSize().toULong()

  actual fun fill(value: Byte, offset: ULong, size: ULong) {
    native.asSlice(offset.toLong(), size.toLong()).fill(value)
  }

  actual fun copyFrom(src: ForeignMemory, srcOffset: ULong, offset: ULong, size: ULong) {
    native.asSlice(offset.toLong(), size.toLong())
      .copyFrom(
        src.native.asSlice(srcOffset.toLong(), size.toLong())
      )
  }

  actual fun getByte(offset: ULong): Byte =
    native.get(ValueLayout.OfByte.JAVA_BYTE, offset.toLong())

  actual fun setByte(offset: ULong, value: Byte) {
    native.set(ValueLayout.OfByte.JAVA_BYTE, offset.toLong(), value)
  }

  actual fun getInt(offset: ULong): Int =
    native.get(ValueLayout.OfInt.JAVA_INT, offset.toLong())

  actual fun setInt(offset: ULong, value: Int) {
    native.set(ValueLayout.OfInt.JAVA_INT, offset.toLong(), value)
  }

  actual fun getShort(offset: ULong): Short =
    native.get(ValueLayout.OfShort.JAVA_SHORT, offset.toLong())

  actual fun setShort(offset: ULong, value: Short) {
    native.set(ValueLayout.OfShort.JAVA_SHORT, offset.toLong(), value)
  }

  actual fun getLong(offset: ULong): Long =
    native.get(ValueLayout.OfLong.JAVA_LONG, offset.toLong())

  actual fun setLong(offset: ULong, value: Long) {
    native.set(ValueLayout.OfLong.JAVA_LONG, offset.toLong(), value)
  }

  actual fun setString(offset: ULong, value: String) {
    native.setUtf8String(offset.toLong(), value)
  }

  actual fun getString(offset: ULong): String =
    native.getUtf8String(offset.toLong())

  actual fun copyFrom(src: ByteArray, srcOffset: ULong, offset: ULong, size: ULong) {
    val srcMem = MemorySegment.ofArray(src).asSlice(srcOffset.toLong(), size.toLong())
    native.asSlice(offset.toLong(), size.toLong()).copyFrom(srcMem)
  }

  actual fun copyTo(dest: ByteArray, destOffset: ULong, offset: ULong, size: ULong) {
    MemorySegment.ofArray(dest).asSlice(destOffset.toLong(), size.toLong())
      .copyFrom(
        native.asSlice(offset.toLong(), size.toLong())
      )
  }
}
