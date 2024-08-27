package pw.binom.wasm

import pw.binom.collections.LinkedList
import pw.binom.copyTo
import pw.binom.io.*
import pw.binom.reverse
import pw.binom.writeInt
import pw.binom.writeLong

val Output.asWasm
  get() = StreamWriter(this)

class StreamWriter(val out: Output) : WasmOutput {
  val callback = LinkedList<String>()
  private var recording = false

  var cursor = 0
    private set
  private val buffer = ByteBuffer(8)

  private inline fun <T> recording(a: () -> T): T {
    recording = true
    val result = a()
    recording = false
    return result
  }

  override fun write(data: ByteBuffer): DataTransferSize {
    val e = out.write(data)
    if (e.isAvailable) {
      val s = Throwable().stackTraceToString()
      if (recording) {
        repeat(e.length) {
          callback += s
        }
      }

      cursor += e.length
    }
    return e
  }

  override fun flush() = out.flush()

  override fun bytes(data: ByteArray) {
    recording {
      writeByteArray(data, buffer)
    }
  }

  override fun bytes(data: ByteBuffer) {
    recording {
      write(data)
    }
  }

  override fun bytes(data: Input) {
    recording {
      data.copyTo(this)
    }
  }

  fun write(byte: UByte) = recording { write(byte.toByte()) }
  fun write(byte: Byte) {
    recording {
      buffer.reset(0, 1)
      buffer[0] = byte
      writeFully(buffer)
    }
  }

  override fun i8s(value: Byte) {
    recording {
      buffer.reset(0, 1)
      buffer[0] = value
      writeFully(buffer)
    }
  }

  override fun i8u(value: UByte) = recording { i8s(value.toByte()) }

  override fun v64u(value: ULong) {
    recording {
      buffer.clear()
      Leb.writeUnsignedLeb128(value = value) { byte ->
        buffer.put(byte)
      }
      buffer.flip()
      writeFully(buffer)
    }
  }

  override fun v64s(value: Long) {
    recording {
      buffer.clear()
      Leb.writeSignedLeb128(value = value) { byte ->
        buffer.put(byte)
      }
      buffer.flip()
      writeFully(buffer)
    }
  }

  override fun i64s(value: Long) {
    recording {
      writeLong(buffer, value.reverse())
    }
  }

  override fun i32s(value: Int) {
    recording {
      writeInt(buffer, value.reverse())
    }
  }

  override fun v32u(value: UInt) {
    recording {
      buffer.clear()
      Leb.writeUnsignedLeb128(value = value.toULong() and 0xFFFFFFFFuL) { byte ->
        buffer.put(byte)
      }
      buffer.flip()
      writeFully(buffer)
    }
  }

  override fun v32s(value: Int) {
    recording {
      buffer.clear()
      Leb.writeSignedLeb128(value = value.toLong() and 0xFFFFFFFFL) { byte ->
        buffer.put(byte)
      }
      buffer.flip()
      writeFully(buffer)
    }
  }

  fun v33u(value: ULong) {
    recording {
      buffer.clear()
      Leb.writeUnsignedLeb128(value = value) { byte ->
        buffer.put(byte)
      }
      buffer.flip()
      writeFully(buffer)
    }
  }

  override fun v33s(value: Long) {
    recording {
      buffer.clear()
      Leb.writeSignedLeb128(value = value) { byte ->
        buffer.put(byte)
      }
      buffer.flip()
      writeFully(buffer)
    }
  }

  override fun string(value: String) {
    val data = value.encodeToByteArray()
    v32u(data.size.toUInt())
    bytes(data)
  }

  override fun close() {
    try {
      out.close()
    } finally {
      buffer.close()
    }
  }

  override fun v1u(b: Boolean) {
    recording {
      write(if (b) 1 else 0)
    }
  }


  private class VectorWriterImpl(val main: WasmOutput) : VectorWriter {

    private var vectorCount = 0
    private val vectorData = ByteArrayOutput()
    private val vectorStream = StreamWriter(ByteArrayOutput())

    override fun element(value: (WasmOutput) -> Unit) {
      vectorCount++
      value(vectorStream)
    }

    override fun close() {
      main.v32u(vectorCount.toUInt())
      vectorData.locked {
        main.write(it)
      }
      vectorData.close()
    }
  }

  override fun vec(): VectorWriter = VectorWriterImpl(this)
}
