package pw.binom.wasm.node.inst

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

data class StructNewDefault(var id:TypeId): Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.structNewDefault(id)
  }
}
