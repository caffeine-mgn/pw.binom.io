package pw.binom.wasm.node.inst

import pw.binom.wasm.node.ValueType
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.ValueVisitor

class SelectWithType : Inst(), ExpressionsVisitor.SelectVisitor {
  val types = ArrayList<ValueType>()
  override fun start() {
    types.clear()
  }

  override fun type(): ValueVisitor {
    val e = ValueType()
    types += e
    return e
  }

  override fun end() {
    super.end()
  }

  override fun accept(visitor: ExpressionsVisitor) {
    val e = visitor.selectWithType()
    e.start()
    types.forEach {
      it.accept(e.type())
    }
    e.end()
  }
}
