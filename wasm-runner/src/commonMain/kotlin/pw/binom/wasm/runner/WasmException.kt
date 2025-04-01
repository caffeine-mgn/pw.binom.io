package pw.binom.wasm.runner

class WasmException(val value: Value) : Exception() {
  override val message: String
    get() = value.toString()
}
