package pw.binom.wasm.node.inst

import pw.binom.wasm.TagId
import pw.binom.wasm.visitors.ExpressionsVisitor

data class ThrowTag(var id: TagId) : Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.throwOp(id)
  }
}
