package pw.binom.wasm.visitors

interface GlobalSectionVisitor {
  companion object {
    val SKIP = object : GlobalSectionVisitor {}
  }

  fun start() {}
  fun end() {}
  fun type(): ValueVisitor = ValueVisitor.SKIP
  fun code(mutable: Boolean): ExpressionsVisitor = ExpressionsVisitor.SKIP
}
