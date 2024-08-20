package pw.binom.wasm.visitors

import pw.binom.wasm.ValueType

interface TypeSectionVisitor {
  companion object {
    val STUB = object : TypeSectionVisitor {}
  }

  fun start() {}
  fun argument(type: ValueType) {}
  fun result(type: ValueType) {}
  fun end() {}
}
