package pw.binom.wasm.nodes.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

class Catch(val value: UInt) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.catch(value)
  }
}
