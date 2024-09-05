package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

data class I32Const(var value: Int) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.const(value)
  }
}
