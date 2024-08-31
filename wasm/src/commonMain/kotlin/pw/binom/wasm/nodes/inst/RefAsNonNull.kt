package pw.binom.wasm.nodes.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

object RefAsNonNull : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.refAsNonNull()
  }
}
