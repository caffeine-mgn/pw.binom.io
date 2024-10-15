package pw.binom.memory

expect class ForeignMemory : AutoCloseable {
  companion object {
    fun allocate(length: ULong): ForeignMemory
  }

  val size: ULong
  fun fill(value: Byte, offset: ULong = 0uL, size: ULong)
  fun copyFrom(src: ForeignMemory, srcOffset: ULong, offset: ULong, size: ULong)
  fun copyFrom(src: ByteArray, srcOffset: ULong, offset: ULong, size: ULong)
  fun copyTo(dest: ByteArray, destOffset: ULong, offset: ULong, size: ULong)
  override fun close()

  fun getByte(offset: ULong): Byte
  fun setByte(offset: ULong, value: Byte)

  fun getShort(offset: ULong): Short
  fun setShort(offset: ULong, value: Short)

  fun getInt(offset: ULong): Int
  fun setInt(offset: ULong, value: Int)

  fun getLong(offset: ULong): Long
  fun setLong(offset: ULong, value: Long)

  fun setString(offset: ULong, value: String)
  fun getString(offset: ULong): String
}
