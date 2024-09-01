package pw.binom.wasm

import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.Output
import pw.binom.io.empty
import kotlin.test.fail

class DiffOutput(val eq: () -> Byte, val len: Int) : Output {
  companion object {
    fun makeEq(array: ByteArray): () -> Byte {
      var cursor = 0
      return {
        val r = array[cursor]
        cursor++
        r
      }
    }
  }

  constructor(data: ByteArray) : this(makeEq(data), data.size)

  private var cursor = 0
  override fun write(data: ByteBuffer): DataTransferSize = if (data.hasRemaining) {
    val r = DataTransferSize.ofSize(data.remaining)
    data.forEach { writeByte ->
      val validByte = eq()
      val a = validByte.toUByte().toString(16).padStart(2, '0')
      val b = writeByte.toUByte().toString(16).padStart(2, '0')
      if (validByte != writeByte) {
        println("0x${cursor.toString(16)} FAIL -> expected: 0x$a, actual: 0x$b")
        fail("Invalid value on 0x${cursor.toString(16)}. expected 0x$a ($validByte), actual: 0x$b ($writeByte)")
      } else {
//        println("0x${cursor.toString(16)} -> 0x$b ($validByte)")
      }
      cursor++
    }
    data.empty()
    r
  } else {
    DataTransferSize.EMPTY
  }

  override fun flush() {
  }

  override fun close() {
    if (cursor != len) {
      throw IllegalStateException("Not all data was wrote. Len $len, wrote $cursor")
    }
  }
}
