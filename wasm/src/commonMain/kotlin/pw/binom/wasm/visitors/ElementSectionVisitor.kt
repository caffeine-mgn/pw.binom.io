package pw.binom.wasm.visitors

import pw.binom.wasm.FunctionId

interface ElementSectionVisitor {
  interface Type0Visitor {
    companion object {
      val SKIP = object : Type0Visitor {}
    }

    fun start() {}
    fun end() {}
    fun exp(): ExpressionsVisitor = ExpressionsVisitor.SKIP
    fun func(id: FunctionId) {}
  }

  companion object {
    val SKIP = object : ElementSectionVisitor {}
  }

  fun start() {}
  fun end() {}
  fun type0(): Type0Visitor = Type0Visitor.SKIP
}
