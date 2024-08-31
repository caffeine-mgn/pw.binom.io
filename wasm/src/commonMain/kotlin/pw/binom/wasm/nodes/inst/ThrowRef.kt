package pw.binom.wasm.nodes.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

object ThrowRef : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.throwRef()
  }
}
