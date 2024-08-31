package pw.binom.wasm.dom.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class F64Const(var value: Double) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.const(value)
  }
}
