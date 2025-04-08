package pw.binom.wasm.node

import pw.binom.wasm.AbsoluteHeapType
import pw.binom.wasm.visitors.ValueVisitor
import kotlin.js.JsName

class RefType : ValueVisitor.RefVisitor {
  companion object {
    fun ref(value: HeapType): RefType {
      val r = RefType()
      r.ref = value
      return r
    }

    fun refNull(value: HeapType): RefType {
      val r = RefType()
      r.refNull = value
      return r
    }

    fun refNullAbs(value: AbsoluteHeapType): RefType {
      val r = RefType()
      r.refNullAbs = value
      return r
    }
  }

  @JsName("refF")
  var ref: HeapType? = null
    set(value) {
      field = value
      if (value != null) {
        refNull = null
        refNullAbs = null
      }
    }

  @JsName("refNullF")
  var refNull: HeapType? = null
    set(value) {
      field = value
      if (value != null) {
        ref = null
        refNullAbs = null
      }
    }
  var refNullAbs: AbsoluteHeapType? = null
    set(value) {
      field = value
      if (value != null) {
        ref = null
        refNull = null
      }
    }

  val heapRef
    get() = ref ?: refNull

  val isNullable
    get() = when {
      refNull != null || refNullAbs != null -> true
      ref != null -> false
      else -> TODO()
    }

  override fun toString(): String {
    ref?.let { return "RefType(ref=$it)" }
    refNull?.let { return "RefType(refNull=$it)" }
    refNullAbs?.let { return "RefType(refNullAbs=$it)" }
    return "RefType()"
  }

  override fun ref(): ValueVisitor.HeapVisitor {
    val e = HeapType()
    refNullAbs = null
    ref = e
    refNull = null
    return e
  }

  override fun refNull(): ValueVisitor.HeapVisitor {
    val e = HeapType()
    refNullAbs = null
    ref = null
    refNull = e
    return e
  }

  override fun refNull(type: AbsoluteHeapType) {
    refNullAbs = type
    ref = null
    refNull = null
  }

  fun accept(visitor: ValueVisitor.RefVisitor) {
    check(ref != null || refNull != null || refNullAbs != null)
    when {
      ref != null -> ref!!.accept(visitor.ref())
      refNull != null -> refNull!!.accept(visitor.refNull())
      refNullAbs != null -> visitor.refNull(refNullAbs!!)
      else -> throw IllegalStateException()
    }
  }
}
