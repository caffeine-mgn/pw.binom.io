package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class ThrowRef : Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.throwRef()
  }
}
