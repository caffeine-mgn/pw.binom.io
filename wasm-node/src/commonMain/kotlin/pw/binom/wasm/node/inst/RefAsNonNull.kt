package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class RefAsNonNull : Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.refAsNonNull()
  }
}
