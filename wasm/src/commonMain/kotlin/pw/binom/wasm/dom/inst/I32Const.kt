package pw.binom.wasm.dom.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class I32Const(var value: Int) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.const(value)
  }
}
