package pw.binom.wasm.runner

import pw.binom.memory.ForeignMemory
import pw.binom.wasm.runner.MemorySpaceByteBuffer.Companion.PAGE_SIZE

class MemorySpaceByMemory(val minSize: Int, val maxSize: Int = Int.MAX_VALUE) : MemorySpace {

  private var data = ForeignMemory.allocate(minSize.toULong())

  override val limit: UInt
    get() = data.size.toUInt()

  override fun grow(mem: UInt): UInt? {
    val newLim = limit.toLong() + (mem.toLong() * PAGE_SIZE.toLong())
    if (newLim > maxSize) return null
    else (limit / PAGE_SIZE).also {
      val newData = ForeignMemory.allocate(newLim.toULong())
      newData.copyFrom(data, srcOffset = 0uL, offset = 9uL, size = data.size)
      data.close()
      data = newData
      return it
    }
  }

  override fun pushI8(value: Byte, offset: UInt, align: UInt) {
    data.setByte(offset = offset.toULong(), value = value)
  }

  override fun getI8(offset: UInt): Byte =
    data.getByte(offset = offset.toULong())

  override fun pushBytes(src: ByteArray, offset: UInt, srcOffset: Int, srcLength: Int) {
    data.copyFrom(
      src = src,
      offset = offset.toULong(),
      srcOffset = srcOffset.toULong(),
      size = srcLength.toULong(),
    )
  }

  override fun getBytes(dest: ByteArray, offset: UInt, destOffset: Int, len: Int) {
    data.copyTo(
      dest = dest,
      destOffset = destOffset.toULong(),
      offset = offset.toULong(),
      size = len.toULong(),
    )
  }
}
