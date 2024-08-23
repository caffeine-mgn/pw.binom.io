package pw.binom.wasm.visitors

import pw.binom.wasm.FunctionId

interface ImportSectionVisitor {
  companion object {
    val STUB = object : ImportSectionVisitor {}
  }

  fun start() {}
  fun end() {}
  fun function(module: String, field: String, index: FunctionId) {}
  fun memory(module: String, field: String, initial: UInt, maximum: UInt) {}
  fun memory(module: String, field: String, initial: UInt) {}
  fun table(module: String, field: String): TableVisitor = TableVisitor.EMPTY
}
