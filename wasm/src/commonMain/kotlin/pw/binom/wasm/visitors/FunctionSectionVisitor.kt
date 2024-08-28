package pw.binom.wasm.visitors

import pw.binom.wasm.FunctionId

interface FunctionSectionVisitor {
  companion object {
    val SKIP = object : FunctionSectionVisitor {}
  }

  fun start() {}
  fun value(int: FunctionId) {}
  fun end() {}
}
