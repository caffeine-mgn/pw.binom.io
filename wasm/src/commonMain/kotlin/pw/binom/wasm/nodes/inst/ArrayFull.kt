package pw.binom.wasm.nodes.inst

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

class ArrayFull(val id:TypeId):Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.arrayFull(id)
  }
}
