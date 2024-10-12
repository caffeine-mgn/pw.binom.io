package pw.binom.wasm.node.inst

import pw.binom.wasm.MemoryId
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed class MemoryOp : Inst() {
  data class Grow(var id: MemoryId) : MemoryOp() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memoryGrow(id)
    }
  }

  data class Size(var id: MemoryId) : MemoryOp() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memorySize(id)
    }
  }
}
