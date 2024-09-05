package pw.binom.wasm.runner

class LocalVar {
  private var value: Any? = null
  fun get() = value
  fun set(value: Any?) {
    this.value = value
  }
}
