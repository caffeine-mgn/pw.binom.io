package pw.binom.wasm.nodes.inst

import pw.binom.wasm.FunctionId
import pw.binom.wasm.visitors.ExpressionsVisitor

class CallFunction(val id: FunctionId) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.call(id)
  }
}
