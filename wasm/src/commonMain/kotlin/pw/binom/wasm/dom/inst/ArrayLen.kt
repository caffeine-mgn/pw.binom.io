package pw.binom.wasm.dom.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

object ArrayLen : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.arrayLen()
  }
}
