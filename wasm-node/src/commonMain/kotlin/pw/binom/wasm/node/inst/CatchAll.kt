package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class CatchAll : Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.catchAll()
  }
}
