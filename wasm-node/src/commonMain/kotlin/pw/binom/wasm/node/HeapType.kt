package pw.binom.wasm.node

import pw.binom.wasm.AbsoluteHeapType
import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ValueVisitor

class HeapType : ValueVisitor.HeapVisitor {
  companion object {
    fun type(value: TypeId): HeapType {
      val r = HeapType()
      r.type = value
      return r
    }
    fun abs(value: AbsoluteHeapType): HeapType {
      val r = HeapType()
      r.abs = value
      return r
    }
  }

  var abs: AbsoluteHeapType? = null
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


  override fun type(type: AbsoluteHeapType) {
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

  override fun toString(): String {
    abs?.let { return "HeapType(abs=$abs)" }
    type?.let { return "HeapType(type=$type)" }
    return "HeapType()"
  }
}
