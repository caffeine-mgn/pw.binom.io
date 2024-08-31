package pw.binom.wasm.dom.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

object RefAsNonNull : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.refAsNonNull()
  }
}
