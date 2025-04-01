package pw.binom.wasm.node.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.node.HeapType
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed class Ref : Inst() {
  abstract var heap: HeapType

  class Null : Ref() {
    override var heap = HeapType()
    override fun accept(visitor: ExpressionsVisitor) {
      heap.accept(visitor.refNull())
    }

    override fun toString(): String = "ref.null"
  }

  class Cast : Ref() {
    override var heap = HeapType()
    override fun accept(visitor: ExpressionsVisitor) {
      heap.accept(visitor.ref(Opcodes.GC_REF_CAST))
    }

    override fun toString(): String = "ref.cast"
  }

  class TestNull : Ref() {
    override var heap = HeapType()
    override fun accept(visitor: ExpressionsVisitor) {
      heap.accept(visitor.ref(Opcodes.GC_REF_TEST_NULL))
    }
  }

  class Test : Ref() {
    override var heap = HeapType()
    override fun accept(visitor: ExpressionsVisitor) {
      heap.accept(visitor.ref(Opcodes.GC_REF_TEST))
    }

    override fun toString(): String = "ref.test"
  }

  class CastNull : Ref() {
    override var heap = HeapType()
    override fun accept(visitor: ExpressionsVisitor) {
      heap.accept(visitor.ref(Opcodes.GC_REF_CAST_NULL))
    }
  }
}
