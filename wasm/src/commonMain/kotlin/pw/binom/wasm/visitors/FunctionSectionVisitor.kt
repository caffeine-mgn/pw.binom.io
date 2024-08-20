package pw.binom.wasm.visitors

interface FunctionSectionVisitor {
  companion object {
    val STUB = object : FunctionSectionVisitor {}
  }

  fun start() {}
  fun value(int: UInt) {}
  fun end() {}
}
