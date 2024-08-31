package pw.binom.wasm.dom.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

interface MemoryOp : Inst {
  class Grow(val size: UInt) : MemoryOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memoryGrow(size)
    }
  }

  class Size(val size: UInt) : MemoryOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memorySize(size)
    }
  }
}
