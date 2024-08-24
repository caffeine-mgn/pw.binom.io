package pw.binom.wasm

import pw.binom.io.ByteArrayOutput

class InMemoryWasmOutput : ByteArrayOutput(), WasmOutput {
  private val stream = StreamWriter(this)
  override fun i8u(value: UByte) {
    stream.i8u(value)
  }

  override fun i8s(value: Byte) {
    stream.i8s(value)
  }

  override fun i32s(value: Int) {
    stream.i32s(value)
  }

  override fun i64s(value: Long) {
    stream.i64s(value)
  }

  override fun v32u(value: UInt) {
    stream.v32u(value)
  }

  override fun v32s(value: Int) {
    stream.v32s(value)
  }

  override fun v64u(value: ULong) {
    stream.v64u(value)
  }

  override fun v64s(value: Long) {
    stream.v64s(value)
  }

  override fun string(value: String) {
    stream.string(value)
  }

  override fun v33s(value: Long) {
    stream.v33s(value)
  }

  override fun v1u(b: Boolean) {
    stream.v1u(b)
  }

  override fun vec(): VectorWriter = stream.vec()
  override fun close() {
    stream.close()
  }
}

fun WasmOutput.write(data: InMemoryWasmOutput) {
  data.locked {
    write(it)
  }
}
