package pw.binom.wasm.nodes.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

object Drop : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.drop()
  }
}
