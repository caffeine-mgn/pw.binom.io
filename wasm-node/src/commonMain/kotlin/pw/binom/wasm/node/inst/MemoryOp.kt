package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

interface MemoryOp : Inst {
  data class Grow(var size: UInt) : MemoryOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memoryGrow(size)
    }
  }

  data class Size(var size: UInt) : MemoryOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memorySize(size)
    }
  }
}
