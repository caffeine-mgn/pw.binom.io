package pw.binom.wasm.nodes.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.nodes.HeapType
import pw.binom.wasm.visitors.ExpressionsVisitor

interface Ref : Inst {
  var heap: HeapType

  class NULL : Ref {
    override var heap = HeapType()
    override fun accept(visitor: ExpressionsVisitor) {
      heap.accept(visitor.refNull())
    }
  }

  class GC_REF_CAST : Ref {
    override var heap = HeapType()
    override fun accept(visitor: ExpressionsVisitor) {
      heap.accept(visitor.ref(Opcodes.GC_REF_CAST))
    }
  }

  class GC_REF_TEST_NULL : Ref {
    override var heap = HeapType()
    override fun accept(visitor: ExpressionsVisitor) {
      heap.accept(visitor.ref(Opcodes.GC_REF_TEST_NULL))
    }
  }

  class GC_REF_TEST : Ref {
    override var heap = HeapType()
    override fun accept(visitor: ExpressionsVisitor) {
      heap.accept(visitor.ref(Opcodes.GC_REF_TEST))
    }
  }

  class GC_REF_CAST_NULL : Ref {
    override var heap = HeapType()
    override fun accept(visitor: ExpressionsVisitor) {
      heap.accept(visitor.ref(Opcodes.GC_REF_CAST_NULL))
    }
  }
}
