package pw.binom.wasm.visitors

interface CodeSectionVisitor {


  interface CodeVisitor {
    companion object {
      val STUB = object : CodeVisitor {}
    }

    fun start() {}
    fun end() {}
    fun local(size: UInt): ValueVisitor = ValueVisitor.SKIP
    fun code(): ExpressionsVisitor = ExpressionsVisitor.SKIP
  }
  companion object {
    val STUB = object : CodeSectionVisitor {}
  }
  fun start() {}
  fun code(): CodeVisitor = CodeVisitor.STUB
  fun end() {}
}
