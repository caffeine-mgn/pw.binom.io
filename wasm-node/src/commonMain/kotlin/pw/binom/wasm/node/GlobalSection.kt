package pw.binom.wasm.node

import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.GlobalSectionVisitor
import pw.binom.wasm.visitors.ValueVisitor

class GlobalSection : GlobalSectionVisitor, MutableList<Global> by ArrayList() {
  private var type: ValueType? = null

  override fun start() {
    clear()
    super.start()
  }

  override fun end() {
    super.end()
  }

  override fun type(): ValueVisitor {
    val e = ValueType()
    type = e
    return e
  }

  override fun code(mutable: Boolean): ExpressionsVisitor {
    val e = Expressions()
    val g = Global(
      type = type!!,
      mutable = mutable,
      expressions = e,
    )
    type = null
    this += g
    return e
  }

  fun accept(visitor: GlobalSectionVisitor) {
    visitor.start()
    forEach {
      it.type.accept(visitor.type())
      it.expressions.accept(visitor.code(it.mutable))
    }
    visitor.end()
  }
}
