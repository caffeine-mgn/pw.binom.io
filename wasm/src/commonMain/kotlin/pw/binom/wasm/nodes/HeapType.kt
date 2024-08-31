package pw.binom.wasm.nodes

import pw.binom.wasm.AbsHeapType
import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ValueVisitor

class HeapType : ValueVisitor.HeapVisitor {
  var abs: AbsHeapType? = null
    set(value) {
      field = value
      if (value != null) {
        type = null
      }
    }
  var type: TypeId? = null
    set(value) {
      field = value
      if (value != null) {
        abs = null
      }
    }

  override fun type(type: AbsHeapType) {
    abs = type
    this.type = null
  }

  override fun type(type: TypeId) {
    abs = null
    this.type = type
  }

  fun accept(visitor: ValueVisitor.HeapVisitor) {
    check(abs != null || type != null)
    when {
      abs != null -> visitor.type(abs!!)
      type != null -> visitor.type(type!!)
      else -> throw IllegalStateException()
    }
  }
}
