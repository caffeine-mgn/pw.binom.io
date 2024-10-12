package pw.binom.wasm.runner

class MemorySpaceByteArray(val minSize: Int, val maxSize: Int = Int.MAX_VALUE) : MemorySpace {
  companion object {
    const val PAGE_SIZE = 65536u
  }

  override val limit
    get() = data.size.toUInt()

  override fun grow(mem: UInt): UInt? {
    val newLim = limit.toLong() + (mem.toLong() * PAGE_SIZE.toLong())
    if (newLim > maxSize) return null
    else (limit / PAGE_SIZE).also {
      val newData = ByteArray(newLim.toInt())
      data.copyInto(newData)
      data = newData
      return it
    }
  }

  override fun getBytes(dest: ByteArray, offset: UInt, destOffset: Int, len: Int) {
    data.copyInto(
      destination = dest,
      destinationOffset = destOffset,
      startIndex = offset.toInt(),
      endIndex = minOf(offset.toInt() + len, data.size)
    )
  }

  private var data = ByteArray(minSize)

  override fun pushI8(value: Byte, offset: UInt, align: UInt) {
    data[offset.toInt()] = value
  }

  override fun pushBytes(src: ByteArray, offset: UInt, srcOffset: Int, srcLength: Int) {
    src.copyInto(
      destination = data,
      destinationOffset = offset.toInt(),
      startIndex = srcOffset,
      endIndex = srcOffset + srcLength
    )
  }

  override fun getI8(offset: UInt) = data[offset.toInt()]
}
