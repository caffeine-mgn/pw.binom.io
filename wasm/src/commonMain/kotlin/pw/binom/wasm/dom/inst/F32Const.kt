package pw.binom.wasm.dom.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class F32Const(var value: Float) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.const(value)
  }
}
