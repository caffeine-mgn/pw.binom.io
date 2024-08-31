package pw.binom.wasm.nodes.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class Rethrow(val value: UInt) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.rethrow(value)
  }
}
