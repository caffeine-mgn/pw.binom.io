package pw.binom.memory

actual class ForeignMemory : AutoCloseable {
  actual companion object {
    actual fun allocate(length: ULong): ForeignMemory = TODO()
  }

  actual val size: ULong
    get() = TODO()

  actual fun fill(value: Byte, offset: ULong, size: ULong):Unit = TODO()
  actual fun copyFrom(src: ForeignMemory, srcOffset: ULong, offset: ULong, size: ULong):Unit = TODO()
  actual fun copyFrom(src: ByteArray, srcOffset: ULong, offset: ULong, size: ULong):Unit = TODO()
  actual fun copyTo(dest: ByteArray, destOffset: ULong, offset: ULong, size: ULong):Unit = TODO()
  actual override fun close():Unit = TODO()

  actual fun getByte(offset: ULong): Byte = TODO()
  actual fun setByte(offset: ULong, value: Byte):Unit = TODO()

  actual fun getShort(offset: ULong): Short = TODO()
  actual fun setShort(offset: ULong, value: Short):Unit = TODO()

  actual fun getInt(offset: ULong): Int = TODO()
  actual fun setInt(offset: ULong, value: Int):Unit = TODO()

  actual fun getLong(offset: ULong): Long = TODO()
  actual fun setLong(offset: ULong, value: Long):Unit = TODO()

  actual fun setString(offset: ULong, value: String):Unit = TODO()
  actual fun getString(offset: ULong): String = TODO()
}
