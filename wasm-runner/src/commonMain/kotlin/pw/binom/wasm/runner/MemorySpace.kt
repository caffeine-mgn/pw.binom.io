package pw.binom.wasm.runner

import pw.binom.fromBytes
import pw.binom.toByteArray

class MemorySpace(val minSize: Int, maxSize: Int = Int.MAX_VALUE) {

  val data = ByteArray(5000)

  fun pushI32(value: Int, offset: UInt, align: UInt) {
    value.toByteArray(
      destination = data,
      offset = offset.toInt(),
    )
  }

  fun pushI64(value: Long, offset: UInt, align: UInt) {
    value.toByteArray(
      destination = data,
      offset = offset.toInt(),
    )
  }

  fun getI32(offset: UInt) = Int.fromBytes(source = data, offset = offset.toInt())
  fun getI64(offset: UInt) = Long.fromBytes(source = data, offset = offset.toInt())
}
