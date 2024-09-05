package pw.binom.wasm.node.inst

import pw.binom.wasm.LabelId
import pw.binom.wasm.visitors.ExpressionsVisitor

class BrTable : ExpressionsVisitor.BrTableVisitor, Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    val default = default
    check(default != null) { "Br table does not have a default branch" }
    val v = visitor.brTable()
    v.start()
    targets.forEach {
      v.target(it)
    }
    v.default(default)
    v.end()
  }


  val targets = ArrayList<LabelId>()
  var default: LabelId? = null

  override fun start() {
    targets.clear()
    default = null
  }

  override fun end() {
    super.end()
  }

  override fun target(label: LabelId) {
    targets += label
  }

  override fun default(label: LabelId) {
    default = label
  }
}
