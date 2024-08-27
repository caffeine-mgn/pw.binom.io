package pw.binom.wasm

import pw.binom.io.ByteArrayOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.Input

class InMemoryWasmOutput : ByteArrayOutput(), WasmOutput {
  private val stream = StreamWriter(this)
  val callback
    get() = stream.callback

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
  override fun bytes(data: ByteArray) = stream.bytes(data)

  override fun bytes(data: ByteBuffer) = stream.bytes(data)

  override fun bytes(data: Input) = stream.bytes(data)

  override fun close() {
    stream.close()
  }

  fun moveTo(out: WasmOutput) {
    when (out) {
      is StreamWriter -> out.callback.addAll(stream.callback)
      is InMemoryWasmOutput -> out.callback.addAll(stream.callback)
      else -> TODO()
    }
    locked {
      out.write(it)
    }
    stream.callback.clear()
    clear()
  }
}
