package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

data object ThrowRef : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.throwRef()
  }
}
