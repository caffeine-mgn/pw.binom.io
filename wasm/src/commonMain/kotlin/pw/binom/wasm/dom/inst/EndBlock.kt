package pw.binom.wasm.dom.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

object EndBlock : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.endBlock()
  }
}
