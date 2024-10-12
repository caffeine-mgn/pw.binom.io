package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class RefIsNull : Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.refIsNull()
  }
}
