package pw.binom.wasm.node.inst

import pw.binom.wasm.FunctionId
import pw.binom.wasm.visitors.ExpressionsVisitor

data class CallFunction(var id: FunctionId) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.call(id)
  }
}
