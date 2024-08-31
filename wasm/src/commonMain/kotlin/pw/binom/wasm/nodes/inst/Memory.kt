package pw.binom.wasm.nodes.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

interface Memory : Inst {
  class Grow(val size: UInt) : Memory {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memoryGrow(size)
    }
  }

  class Size(val size: UInt) : Memory {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memorySize(size)
    }
  }
}
