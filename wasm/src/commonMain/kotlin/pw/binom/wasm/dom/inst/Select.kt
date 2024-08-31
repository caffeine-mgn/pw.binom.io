package pw.binom.wasm.dom.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

object Select : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.select()
  }
}
