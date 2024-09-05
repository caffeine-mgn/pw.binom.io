package pw.binom.wasm.node.inst

import pw.binom.wasm.DataId
import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

interface NewArray : Inst {
  data class Data(var id: TypeId, var data: DataId) : NewArray {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.newArray(type = id, data = data)
    }
  }

  data class Size(var id: TypeId, var size: UInt) : NewArray {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.newArray(type = id, size = size)
    }
  }
}
