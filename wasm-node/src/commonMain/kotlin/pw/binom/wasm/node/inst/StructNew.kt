package pw.binom.wasm.node.inst

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

data class StructNew(var id:TypeId): Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.structNew(id)
  }
}
