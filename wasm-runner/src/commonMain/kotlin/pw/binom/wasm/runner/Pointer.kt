package pw.binom.wasm.runner

class Pointer(val offset: UInt, val memory: MemorySpace) {
  fun loadLong(shift: UInt = 0u) = memory.getI64(offset + shift)
  fun storeLong(value: Long, shift: UInt = 0u) {
    memory.pushI64(value = value, offset = offset + shift, align = 1u)
  }

  fun loadULong(shift: UInt = 0u) = loadLong(shift = shift).toULong()
  fun storeULong(value: ULong, shift: UInt = 0u) {
    storeLong(value = value.toLong(), shift = shift)
  }

  fun loadInt(shift: UInt = 0u) = memory.getI32(offset = offset + shift, align = 1u)
  fun storeInt(value: Int, shift: UInt = 0u) {
    memory.pushI32(value = value, offset = offset + shift, align = 1u)
  }

  fun loadUInt(shift: UInt = 0u) = loadInt(shift = shift).toUInt()
  fun storeUInt(value: UInt, shift: UInt = 0u) {
    storeInt(value = value.toInt(), shift = shift)
  }

  fun loadByte(shift: UInt = 0u) = memory.getI8(offset = offset + shift)
  fun storeByte(value: Byte, shift: UInt = 0u) {
    memory.pushI8(value = value, offset = offset + shift, align = 1u)
  }

  fun loadUByte(shift: UInt = 0u) = loadByte(shift = shift).toUByte()
  fun storeUByte(value: UByte, shift: UInt = 0u) {
    storeByte(value = value.toByte(), shift = shift)
  }

  fun loadShort(shift: UInt = 0u) = memory.getI16(offset = offset + shift, align = 1u)
  fun storeShort(value: Short, shift: UInt = 0u) {
    memory.pushI16(value = value, offset = offset + shift, align = 1u)
  }

  fun loadUShort(shift: UInt = 0u) = loadShort(shift = shift).toUShort()
  fun storeUShort(value: UShort, shift: UInt = 0u) {
    storeShort(value = value.toShort(), shift = shift)
  }

  fun withOffset(shift: UInt = 0u) =
    Pointer(offset = offset + shift, memory = memory)
}
