package pw.binom.wasm.node.inst

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

data class ArrayCopy(var from: TypeId, var to: TypeId) : Inst() {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.arrayCopy(from = from, to = to)
  }

  override fun toString(): String = "array.copy ${from.value} ${to.value}"
}
