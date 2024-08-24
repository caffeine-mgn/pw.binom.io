package pw.binom.wasm

import pw.binom.io.Output

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
}
