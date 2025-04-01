package pw.binom.wasm.node.inst

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

data class StructNew(var structTypeId:TypeId): Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.structNew(structTypeId)
  }

  override fun toString() = "struct.new ${structTypeId.value}"
}
