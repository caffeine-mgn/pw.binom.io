package pw.binom.wasm

object WasmIO {
  inline fun v32u(firstByte: Byte, nextByte: () -> Byte): UInt {
    var first = false
    return Leb.readUnsigned(maxBits = 32) {
      if (!first) {
        first = true
        firstByte
      } else {
        nextByte()
      }
    }.toUInt()
  }

  inline fun v32s(nextByte: () -> Byte) = Leb.readSigned(maxBits = 32, readByte = nextByte).toInt()

  inline fun v32u(value: UInt, put: (Byte) -> Unit) {
    Leb.writeUnsignedLeb1282(value = value.toULong()) { byte ->
      put(byte)
    }
  }

  inline fun v32s(value: Int, put: (Byte) -> Unit) {
    Leb.writeSignedLeb128(value = value.toLong()) { byte ->
      put(byte)
    }
  }
}
