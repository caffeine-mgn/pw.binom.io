package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

data class F32Const(var value: Float) : Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.const(value)
  }
}
