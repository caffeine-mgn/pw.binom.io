package pw.binom.wasm.visitors

interface CodeSectionVisitor {
  companion object {
    val STUB = object : CodeSectionVisitor {}
  }

  fun start() {}
  fun end() {}
  fun local(size: UInt): ValueVisitor = ValueVisitor.SKIP
  fun code(): ExpressionsVisitor = ExpressionsVisitor.SKIP
}
