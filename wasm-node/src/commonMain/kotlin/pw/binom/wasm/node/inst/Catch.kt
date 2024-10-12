package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

data class Catch(var value: UInt) : Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.catch(value)
  }
}
