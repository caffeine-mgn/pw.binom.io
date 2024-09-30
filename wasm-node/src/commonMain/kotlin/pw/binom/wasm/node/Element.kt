package pw.binom.wasm.node

import pw.binom.wasm.FunctionId
import pw.binom.wasm.visitors.ElementSectionVisitor
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface Element {
  fun accept(visitor: ElementSectionVisitor)
    class Type0 : Element, ElementSectionVisitor.Type0Visitor {
    val expressions = Expressions()
    val functions = ArrayList<FunctionId>()

    override fun start() {
      functions.clear()
      expressions.clear()
    }

    override fun exp() = expressions

    override fun func(id: FunctionId) {
      functions += id
    }

    override fun accept(visitor: ElementSectionVisitor) {
      accept(visitor.type0())
    }
    fun accept(visitor: ElementSectionVisitor.Type0Visitor) {
      visitor.start()
      expressions.accept(visitor.exp())
      functions.forEach {
        visitor.func(it)
      }
      visitor.end()
    }
  }
}
