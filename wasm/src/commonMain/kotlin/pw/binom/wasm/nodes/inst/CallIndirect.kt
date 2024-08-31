package pw.binom.wasm.nodes.inst

import pw.binom.wasm.TableId
import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

class CallIndirect(val type: TypeId, val table: TableId) : Inst {
  override fun accept(visitor: ExpressionsVisitor) {
    visitor.callIndirect(type = type, table = table)
  }
}
