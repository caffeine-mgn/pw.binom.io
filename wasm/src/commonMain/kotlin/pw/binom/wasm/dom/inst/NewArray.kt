package pw.binom.wasm.dom.inst

import pw.binom.wasm.DataId
import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

interface NewArray : Inst {
  class Data(val id: TypeId, val data: DataId) : NewArray {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.newArray(type = id, data = data)
    }
  }

  class Size(val id: TypeId, val size: UInt) : NewArray {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.newArray(type = id, size = size)
    }
  }
}
