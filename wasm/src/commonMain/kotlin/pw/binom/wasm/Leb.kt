package pw.binom.wasm

object Leb {
  inline fun readUnsigned(maxBits: Int, readByte: () -> Byte): ULong {
    var maxBytes = (maxBits + 6) / 7
    var shift = 0
    var result = 0uL
    var byte: UByte
    while (true) {
      byte = readByte().toUByte();
      result = result or (((byte and 0x7fu).toULong()) shl shift);
      if ((byte and 0x80u) == 0.toUByte()) {
        break
      }
      shift += 7
      maxBytes--
      if (maxBytes <= 0) {
        break
      }
    }
    return result
  }

  inline fun readSigned(maxBits: Int, readByte: () -> Byte): Long {
    var maxBytes = (maxBits + 6) / 7
    var shift = 0
    var result = 0L
    var byte: UByte

    while (true) {
      byte = readByte().toUByte()
      result = result or (((byte and 0x7fu).toULong()) shl shift).toLong();
      shift += 7;
      if ((byte and 0x80u.toUByte()) == 0u.toUByte())
        break
      maxBytes--
      if (maxBytes <= 0) {
        break
      }
    }
    if (shift < (Long.SIZE_BYTES * 8) && (byte and 0x40u) != 0u.toUByte())
      result = result or (-(1.toLong() shl shift))

    return result
  }

  inline fun writeUnsignedLeb128(value: ULong, write: (Byte) -> Unit) {
    var value = value.toLong()
    var remaining = value ushr 7

    while (remaining != 0L) {
      write(((value and 0x7f) or 0x80).toByte())
      value = remaining
      remaining = remaining ushr 7
    }

    write((value and 0x7f).toByte())
  }

  inline fun writeSignedLeb128(value: Long, write: (Byte) -> Unit) {
    var value = value
    var remaining = value shr 7
    var hasMore = true
    var end = if ((value and Long.MIN_VALUE) == 0L) 0L else -1L

    while (hasMore) {
      hasMore = (remaining != end)
        || ((remaining and 1) != ((value shr 6) and 1));

      write(((value and 0x7f) or (if (hasMore) 0x80 else 0)).toByte())
      value = remaining
      remaining = remaining shr 7
    }
  }
}
