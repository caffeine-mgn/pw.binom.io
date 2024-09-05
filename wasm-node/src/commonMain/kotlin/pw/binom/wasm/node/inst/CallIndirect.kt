package pw.binom.wasm.node.inst

import pw.binom.wasm.TableId
import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

data class CallIndirect(var type: TypeId, var table: TableId) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.callIndirect(type = type, table = table)
  }
}
