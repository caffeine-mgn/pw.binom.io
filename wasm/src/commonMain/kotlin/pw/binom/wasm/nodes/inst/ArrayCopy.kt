package pw.binom.wasm.nodes.inst

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

class ArrayCopy(val from: TypeId, val to: TypeId) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.arrayCopy(from = from, to = to)
  }
}
