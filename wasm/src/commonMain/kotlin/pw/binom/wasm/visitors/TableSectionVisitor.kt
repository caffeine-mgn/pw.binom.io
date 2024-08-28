package pw.binom.wasm.visitors

interface TableSectionVisitor {
  companion object {
    val SKIP = object : TableSectionVisitor {}
  }

  fun start() {}
  fun end() {}
  fun type(): ValueVisitor.RefVisitor = ValueVisitor.RefVisitor.SKIP
  fun limit(inital: UInt) {}
  fun limit(inital: UInt, max: UInt) {}
}
