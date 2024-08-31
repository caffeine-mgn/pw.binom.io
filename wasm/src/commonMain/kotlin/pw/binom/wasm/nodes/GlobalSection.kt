package pw.binom.wasm.nodes

import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.GlobalSectionVisitor
import pw.binom.wasm.visitors.ValueVisitor

class GlobalSection : GlobalSectionVisitor {
  val elements = ArrayList<Global>()
  private var type: ValueType? = null

  override fun start() {
    elements.clear()
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
    elements += g
    return e
  }

  fun accept(visitor: GlobalSectionVisitor) {
    visitor.start()
    elements.forEach {
      it.type.accept(visitor.type())
      it.expressions.accept(visitor.code(it.mutable))
    }
    visitor.end()
  }
}
