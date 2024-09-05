package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

data object EndBlock : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.endBlock()
  }
}
