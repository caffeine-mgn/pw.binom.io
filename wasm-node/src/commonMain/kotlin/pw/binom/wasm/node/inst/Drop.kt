package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class Drop : Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.drop()
  }
}
