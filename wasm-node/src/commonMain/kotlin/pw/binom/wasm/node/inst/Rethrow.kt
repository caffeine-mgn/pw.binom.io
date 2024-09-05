package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

data class Rethrow(var value: UInt) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.rethrow(value)
  }
}
