package pw.binom.wasm.node.inst

import pw.binom.wasm.FunctionId
import pw.binom.wasm.visitors.ExpressionsVisitor

data class RefFunction(var id: FunctionId) : Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.ref(id)
  }
}
