package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class Select : Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.select()
  }
}
