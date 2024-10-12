package pw.binom.wasm.runner

import pw.binom.eachByteIndexed
import pw.binom.fromBytes
import pw.binom.reverse
import pw.binom.toByteArray

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

  var data = ByteArray(minSize)

  override fun pushI8(value: Byte, offset: UInt, align: UInt) {
    data[offset.toInt()] = value
  }

//  override fun pushI16(value: Short, offset: UInt, align: UInt) {
//    value.eachByteIndexed { byte, index ->
//      data[offset.toInt() + (1 - index)] = byte
//    }
//  }

  override fun pushBytes(src: ByteArray, offset: UInt, srcOffset: Int, srcLength: Int) {
    src.copyInto(
      destination = data,
      destinationOffset = offset.toInt(),
      startIndex = srcOffset,
      endIndex = srcOffset + srcLength
    )
  }

//  override fun pushI32(value: Int, offset: UInt, align: UInt) {
//    value.eachByteIndexed { byte, index ->
//      data[offset.toInt() + (3 - index)] = byte
//    }
//  }

//  override fun pushI64(value: Long, offset: UInt, align: UInt) {
//    value.eachByteIndexed { byte, index ->
//      data[offset.toInt() + (7 - index)] = byte
//    }
//  }

  override fun getI8(offset: UInt) = data[offset.toInt()]
}
