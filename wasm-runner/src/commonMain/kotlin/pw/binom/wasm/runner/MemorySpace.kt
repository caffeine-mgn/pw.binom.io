package pw.binom.wasm.runner

import pw.binom.fromBytes
import pw.binom.wasm.MemoryId

interface MemorySpace {

  companion object {
    fun calculateOffset(offset:UInt,align:UInt):UInt{
      if (align == 0u) {
        throw IllegalArgumentException("Align must be positive.");
      }

      val remainder = offset % align

      if (remainder != 0u) {
        val quotient = offset / align
        return (quotient + 1u) * align
      } else {
        return offset
      }
    }
  }

  val limit: UInt

  fun grow(mem: UInt): UInt?
  fun pushI8(value: Byte, offset: UInt, align: UInt)
  fun getI8(offset: UInt): Byte

  fun pushI16(value: Short, offset: UInt, align: UInt) // {
//    val offset = calculateOffset(offset,align)
//    value.eachByteIndexed { byte, index ->
//      pushI8(value = byte, offset = offset + (1u - index.toUInt()), align = 1u)
//    }
//  }

  fun getI16(offset: UInt, align: UInt) :Short{
    val offset = calculateOffset(offset,align)
    return Short.fromBytes { index ->
      getI8(offset + (1u - index.toUInt()))
    }
  }

  fun getI32(offset: UInt, align: UInt): Int // {
//    val offset = calculateOffset(offset,align)
//    return Int.fromBytes { index ->
//      getI8(offset + (3u - index.toUInt()))
//    }
//  }
//
  fun pushI32(value: Int, offset: UInt, align: UInt) // {
//    val offset = calculateOffset(offset,align)
//    value.eachByteIndexed { byte, index ->
//      pushI8(value = byte, offset + (3u - index.toUInt()), align = 1u)
//    }
//  }

  fun getI64(offset: UInt): Long = Long.fromBytes { index ->
    getI8(offset + (7u - index.toUInt()))
  }

  fun pushI64(value: Long, offset: UInt, align: UInt) // {
//    val offset = calculateOffset(offset,align)
//    value.eachByteIndexed { byte, index ->
//      pushI8(value = byte, offset + (7u - index.toUInt()), align = 1u)
//    }
//  }

  fun pushBytes(src: ByteArray, offset: UInt, srcOffset: Int = 0, srcLength: Int = src.size - srcOffset)
  fun pushBytesWithZero(value: ByteArray, offset: UInt) {
    pushBytes(src = value, offset = offset)
    pushI8(value = 0, offset = value.size.toUInt() + offset + 1u, align = 1u)
  }

  fun getBytes(dest: ByteArray, offset: UInt, destOffset: Int = 0, len: Int = dest.size - destOffset)
  fun getBytes(offset: UInt, destOffset: Int = 0, len: Int): ByteArray {
    val result = ByteArray(len)
    getBytes(dest = result, offset = offset)
    return result
  }
}

operator fun List<MemorySpace>.get(index: MemoryId) =
  this[index.raw.toInt()]
