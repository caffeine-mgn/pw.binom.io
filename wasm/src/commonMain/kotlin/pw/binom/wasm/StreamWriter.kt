package pw.binom.wasm

import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.Output
import pw.binom.io.writeByteArray

class StreamWriter(val out: Output) : Output {

  var cursor = 0
    private set
  private val buffer = ByteBuffer(8)

  override fun write(data: ByteBuffer): DataTransferSize {
    val e = out.write(data)
    if (e.isAvailable) {
      cursor += e.length
    }
    return e
  }

  override fun flush() = out.flush()

  fun write(byte: UByte) = write(byte.toByte())
  fun write(byte: Byte) {
    buffer.clear()
    buffer[0] = byte
    writeFully(buffer)
  }

  fun v64u(value: ULong) {
    buffer.clear()
    Leb.writeUnsignedLeb128(value = value) { byte ->
      buffer.put(byte)
    }
    buffer.flip()
    writeFully(buffer)
  }

  fun v64s(value: Long) {
    buffer.clear()
    Leb.writeSignedLeb128(value = value) { byte ->
      buffer.put(byte)
    }
    buffer.flip()
    writeFully(buffer)
  }

  fun v32u(value: UInt) {
    buffer.clear()
    Leb.writeUnsignedLeb128(value = value.toULong()) { byte ->
      buffer.put(byte)
    }
    buffer.flip()
    writeFully(buffer)
  }

  fun v32s(value: Int) {
    buffer.clear()
    Leb.writeSignedLeb128(value = value.toLong()) { byte ->
      buffer.put(byte)
    }
    buffer.flip()
    writeFully(buffer)
  }

  fun v33u(value: ULong) {
    buffer.clear()
    Leb.writeUnsignedLeb128(value = value) { byte ->
      buffer.put(byte)
    }
    buffer.flip()
    writeFully(buffer)
  }

  fun v33s(value: Long) {
    buffer.clear()
    Leb.writeSignedLeb128(value = value) { byte ->
      buffer.put(byte)
    }
    buffer.flip()
    writeFully(buffer)
  }

  fun string(value: String) {
    val data = value.encodeToByteArray()
    v32u(data.size.toUInt())
    writeByteArray(data, buffer)
  }

  fun limit(inital: UInt) {
    v1u(false)
    v32u(inital)
  }

  fun limit(inital: UInt, max: UInt) {
    v1u(true)
    v32u(inital)
    v32u(max)
  }

  override fun close() {
    try {
      out.close()
    } finally {
      buffer.close()
    }
  }

  fun v1u(b: Boolean) {
    write(if (b) 1 else 0)
  }
}
