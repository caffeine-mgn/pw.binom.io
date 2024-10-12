package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class EndBlock : Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.endBlock()
  }
}
