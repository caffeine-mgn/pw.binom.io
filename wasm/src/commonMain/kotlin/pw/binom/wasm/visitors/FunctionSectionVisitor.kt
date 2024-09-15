package pw.binom.wasm.visitors

import pw.binom.wasm.TypeId

interface FunctionSectionVisitor {
  companion object {
    val SKIP = object : FunctionSectionVisitor {}
  }

  fun start() {}
  fun value(int: TypeId) {}
  fun end() {}
}
