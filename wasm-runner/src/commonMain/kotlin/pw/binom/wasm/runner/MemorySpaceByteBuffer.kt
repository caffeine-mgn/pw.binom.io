package pw.binom.wasm.runner

import pw.binom.*
import pw.binom.io.ByteBuffer
import pw.binom.io.holdState

class MemorySpaceByteBuffer(val minSize: Int, val maxSize: Int = Int.MAX_VALUE) : MemorySpace {
  companion object {
    const val PAGE_SIZE = 65536u
  }

  private var data = ByteBuffer(minSize)

  override val limit
    get() = data.capacity.toUInt()

  override fun grow(mem: UInt): UInt? {
    val newLim = limit.toLong() + (mem.toLong() * PAGE_SIZE.toLong())
    if (newLim > maxSize) return null
    else (limit / PAGE_SIZE).also {
      data = data.realloc(newLim.toInt())
      return it
    }
  }

  override fun getBytes(dest: ByteArray, offset: UInt, destOffset: Int, len: Int) {
    data.holdState {
      data.position = offset.toInt()
      data.readInto(dest = dest, offset = destOffset, length = len)
    }
  }

  override fun pushI8(value: Byte, offset: UInt, align: UInt) {
    data[offset.toInt()] = value
  }

  override fun pushBytes(src: ByteArray, offset: UInt, srcOffset: Int, srcLength: Int) {
    data.holdState {
      data.position = offset.toInt()
      data.write(data = src, offset = srcOffset, length = srcLength)
    }
  }

  override fun getI8(offset: UInt) = data[offset.toInt()]

  override fun getI32(offset: UInt): Int =
    data.holdState {
      data.position = offset.toInt()
      data.readInt().reverse()
    }

  override fun pushI32(value: Int, offset: UInt, align: UInt) {
    data.holdState {
      data.position = offset.toInt()
      data.writeInt(value.reverse())
    }
  }

  override fun getI64(offset: UInt): Long = data.holdState {
    data.position = offset.toInt()
    data.readLong().reverse()
  }

  override fun pushI64(value: Long, offset: UInt, align: UInt) {
    data.holdState {
      data.position = offset.toInt()
      data.writeLong(value.reverse())
    }
  }
}
