package pw.binom.wasm.nodes.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

object ArrayLen : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.arrayLen()
  }
}
