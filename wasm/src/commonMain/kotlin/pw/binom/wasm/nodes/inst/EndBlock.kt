package pw.binom.wasm.nodes.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

object EndBlock : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.endBlock()
  }
}
