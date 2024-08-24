package pw.binom.wasm.visitors

interface GlobalSectionVisitor {
  fun start() {}
  fun end() {}
  fun type(): ValueVisitor = ValueVisitor.SKIP
  fun code(mutable: Boolean): ExpressionsVisitor = ExpressionsVisitor.SKIP
}
