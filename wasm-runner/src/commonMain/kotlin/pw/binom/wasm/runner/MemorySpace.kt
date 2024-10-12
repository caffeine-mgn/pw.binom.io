package pw.binom.wasm.runner

import pw.binom.eachByteIndexed
import pw.binom.fromBytes

interface MemorySpace {
  val limit: UInt

  fun grow(mem: UInt): UInt?
  fun pushI8(value: Byte, offset: UInt, align: UInt)
  fun getI8(offset: UInt): Byte


  fun pushI16(value: Short, offset: UInt, align: UInt) {
    value.eachByteIndexed { byte, index ->
      pushI8(value = byte, offset = offset + (1u - index.toUInt()), align = 1u)
    }
  }

  fun getI16(offset: UInt) = Short.fromBytes { index ->
    getI8(offset + (1u - index.toUInt()))
  }

  fun getI32(offset: UInt): Int = Int.fromBytes { index ->
    getI8(offset + (3u - index.toUInt()))
  }
  fun pushI32(value: Int, offset: UInt, align: UInt) {
    value.eachByteIndexed { byte, index ->
      pushI8(value = byte, offset + (3u - index.toUInt()), align = 1u)
    }
  }

  fun getI64(offset: UInt): Long = Long.fromBytes { index ->
    getI8(offset + (7u - index.toUInt()))
  }

  fun pushI64(value: Long, offset: UInt, align: UInt) {
    value.eachByteIndexed { byte, index ->
      pushI8(value = byte, offset + (7u - index.toUInt()), align = 1u)
    }
  }

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
