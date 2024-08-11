package pw.binom.wasm

object Leb {
  inline fun readUnsigned(readByte: () -> Byte): ULong {
    var shift = 0
    var result = 0uL
    var byte: UByte

    while (true) {
      byte = readByte().toUByte();
      result = result or (((byte and 0x7fu).toULong()) shl shift);
      if ((byte and 0x80u) == 0.toUByte()) {
        break
      }
      shift += 7;
    }

    return result
  }

  inline fun readSigned(readByte: () -> Byte): Long {
    var shift = 0
    var result = 0L
    var byte: UByte

    while (true) {
      byte = readByte().toUByte()
      result = result or (((byte and 0x7fu).toULong()) shl shift).toLong();
      shift += 7;
      if ((byte and 0x80u.toUByte()) == 0u.toUByte())
        break;
    }
    if (shift < (Long.SIZE_BYTES * 8) && (byte and 0x40u) != 0u.toUByte())
      result = result or (-(1.toLong() shl shift))

    return result;
  }
}
