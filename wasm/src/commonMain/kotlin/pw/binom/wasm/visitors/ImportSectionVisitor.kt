package pw.binom.wasm.visitors

import pw.binom.wasm.TypeId

interface ImportSectionVisitor {
  companion object {
    val SKIP = object : ImportSectionVisitor {}
  }

  interface GlobalVisitor {
    companion object {
      val SKIP = object : GlobalVisitor {}
    }

    fun start() {}
    fun type(): ValueVisitor = ValueVisitor.SKIP
    fun mutable(value: Boolean) {}
    fun end() {}
  }

  fun start() {}
  fun end() {}
  fun function(module: String, field: String, index: TypeId) {}
  fun memory(module: String, field: String, initial: UInt, maximum: UInt) {}
  fun memory(module: String, field: String, initial: UInt) {}
  fun table(module: String, field: String): TableVisitor = TableVisitor.SKIP
  fun global(module: String, field: String): GlobalVisitor = GlobalVisitor.SKIP
}
