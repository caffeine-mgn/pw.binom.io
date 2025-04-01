package pw.binom.wasm.node.inst

import pw.binom.wasm.FunctionId
import pw.binom.wasm.TableId
import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed class Call:Inst() {
  data class ById(var id: FunctionId) : Call() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.call(id)
    }

    override fun toString(): String = "call ${id.id}"
  }

  data class ByRef(var id: TypeId) : Call() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.call(id)
    }
  }

  data class Indirect(var type: TypeId, var table: TableId) : Call() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.callIndirect(type = type, table = table)
    }
  }
}
