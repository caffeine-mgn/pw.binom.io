package pw.binom.wasm.dom.inst

import pw.binom.wasm.TagId
import pw.binom.wasm.visitors.ExpressionsVisitor

class ThrowTag(val id: TagId) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.throwOp(id)
  }
}
