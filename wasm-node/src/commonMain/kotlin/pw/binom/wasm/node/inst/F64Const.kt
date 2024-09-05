package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

data class F64Const(var value: Double) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.const(value)
  }
}
