package pw.binom.wasm

import pw.binom.io.ByteArrayOutput
import pw.binom.io.Closeable
import pw.binom.io.Output
import pw.binom.io.use

interface WasmOutput : Output {
  fun i8u(value: UByte)
  fun i8s(value: Byte)
  fun i32s(value: Int)
  fun i64s(value: Long)
  fun v32u(value: UInt)
  fun v32s(value: Int)
  fun v64u(value: ULong)
  fun v64s(value: Long)
  fun string(value: String)
  fun v33s(value: Long)
  fun v1u(b: Boolean)
  fun vec(): VectorWriter
}

interface VectorWriter : Closeable {
  fun element(value: (WasmOutput) -> Unit)
}

fun WasmOutput.limit(inital: UInt) {
  v1u(false)
  v32u(inital)
}

fun WasmOutput.limit(inital: UInt, max: UInt) {
  v1u(true)
  v32u(inital)
  v32u(max)
}

inline fun WasmOutput.writeVec(func: (() -> WasmOutput) -> Unit) {
  val data = ByteArrayOutput()
  var count = 0
  StreamWriter(data).use { stream ->
    func {
      count++
      stream
    }
    v32s(count)
    data.locked {
      write(it)
    }
  }
}
