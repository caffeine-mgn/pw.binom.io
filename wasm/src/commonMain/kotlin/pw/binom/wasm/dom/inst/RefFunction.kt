package pw.binom.wasm.dom.inst

import pw.binom.wasm.FunctionId
import pw.binom.wasm.visitors.ExpressionsVisitor

class RefFunction(val id: FunctionId) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.ref(id)
  }
}
