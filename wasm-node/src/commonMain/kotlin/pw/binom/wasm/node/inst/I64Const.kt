package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

data class I64Const(var value: Long) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.const(value)
  }
}
