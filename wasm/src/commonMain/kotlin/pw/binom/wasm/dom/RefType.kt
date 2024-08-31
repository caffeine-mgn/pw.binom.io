package pw.binom.wasm.dom

import pw.binom.wasm.AbsHeapType
import pw.binom.wasm.visitors.ValueVisitor
import kotlin.js.JsName

class RefType : ValueVisitor.RefVisitor {
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
  var refNullAbs: AbsHeapType? = null
    set(value) {
      field = value
      if (value != null) {
        ref = null
        refNull = null
      }
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

  override fun refNull(type: AbsHeapType) {
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
