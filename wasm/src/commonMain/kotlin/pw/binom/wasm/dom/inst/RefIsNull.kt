package pw.binom.wasm.dom.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

object RefIsNull : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.refIsNull()
  }
}
