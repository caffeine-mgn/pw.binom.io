package pw.binom.wasm.nodes.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class I64Const(var value: Long) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.const(value)
  }
}
