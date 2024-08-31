package pw.binom.wasm.dom.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

object ThrowRef : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.throwRef()
  }
}
