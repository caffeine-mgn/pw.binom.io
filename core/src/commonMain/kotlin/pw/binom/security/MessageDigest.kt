package pw.binom.security

import pw.binom.io.ByteBuffer

interface MessageDigest {
  fun init()
  fun update(byte: Byte)
  fun update(input: ByteArray, offset: Int, len: Int) {
    for (i in 0 until len) {
      update(input[i + offset])
    }
  }

  fun update(byte: ByteArray) {
    update(input = byte, offset = 0, len = byte.size)
  }

  fun update(buffer: ByteBuffer) {
    while (buffer.remaining > 0) {
      update(buffer.getByte())
    }
  }

  fun finish(): ByteArray
}
