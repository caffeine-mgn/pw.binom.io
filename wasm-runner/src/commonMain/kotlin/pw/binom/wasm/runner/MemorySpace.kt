package pw.binom.wasm.runner

import pw.binom.fromBytes
import pw.binom.reverse
import pw.binom.toByteArray

class MemorySpace(val minSize: Int, maxSize: Int = Int.MAX_VALUE) {

  val data = ByteArray(minSize)

  fun pushI8(value: Byte, offset: UInt, align: UInt) {
    try {
      data[offset.toInt()]=value
    } catch (e: Throwable) {
      throw RuntimeException("Can't put i32 $value on offset $offset. maxSize: ${data.size}", e)
    }
  }

  fun pushI32(value: Int, offset: UInt, align: UInt) {
    try {
      value.reverse().toByteArray(
        destination = data,
        offset = offset.toInt(),
      )
    } catch (e: Throwable) {
      throw RuntimeException("Can't put i32 $value on offset $offset. maxSize: ${data.size}", e)
    }
  }

  fun pushI64(value: Long, offset: UInt, align: UInt) {
    value.reverse().toByteArray(
      destination = data,
      offset = offset.toInt(),
    )
  }

  fun getI8(offset: UInt) = data[offset.toInt()]
  fun getI32(offset: UInt) = Int.fromBytes(source = data, offset = offset.toInt()).reverse()
  fun getI64(offset: UInt) = Long.fromBytes(source = data, offset = offset.toInt()).reverse()
}
