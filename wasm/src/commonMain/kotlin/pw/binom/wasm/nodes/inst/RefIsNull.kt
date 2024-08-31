package pw.binom.wasm.nodes.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

object RefIsNull : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.refIsNull()
  }
}
