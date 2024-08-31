package pw.binom.wasm.nodes.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

object CatchAll : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.catchAll()
  }
}
