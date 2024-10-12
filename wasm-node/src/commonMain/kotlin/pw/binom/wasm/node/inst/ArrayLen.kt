package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class ArrayLen : Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.arrayLen()
  }
}
