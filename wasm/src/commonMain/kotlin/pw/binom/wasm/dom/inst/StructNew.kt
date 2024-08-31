package pw.binom.wasm.dom.inst

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

class StructNew(val id:TypeId):Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.structNew(id)
  }
}
