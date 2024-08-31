package pw.binom.wasm.dom.inst

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

class CallType(val id: TypeId) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.call(id)
  }
}
