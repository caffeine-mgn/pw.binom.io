package pw.binom.wasm.dom

import pw.binom.wasm.AbsHeapType
import pw.binom.wasm.Primitive
import pw.binom.wasm.Vector
import pw.binom.wasm.visitors.ValueVisitor

class ValueType : ValueVisitor {
  var number: NumberType? = null
    set(value) {
      field = value
      if (value != null) {
        abs = null
        ref = null
        vector = null
      }
    }
  var abs: AbsHeapType? = null
    set(value) {
      field = value
      if (value != null) {
        number = null
        ref = null
        vector = null
      }
    }
  var ref: RefType? = null
    set(value) {
      field = value
      if (value != null) {
        number = null
        abs = null
        vector = null
      }
    }
  var vector: VectorType? = null
    set(value) {
      field = value
      if (value != null) {
        number = null
        abs = null
        ref = null
      }
    }

  override fun numType(): ValueVisitor.NumberVisitor {
    val e = NumberType(Primitive.I32)
    number = e
    return e
  }

  override fun refType(): ValueVisitor.RefVisitor {
    val e = RefType()
    ref = e
    return e
  }

  override fun refType(type: AbsHeapType) {
    abs = type
  }

  override fun vecType(): ValueVisitor.VectorVisitor {
    val e = VectorType(Vector.V128)
    vector = e
    return e
  }

  fun accept(visitor: ValueVisitor) {
    check(number != null || abs != null || ref != null || vector != null)
    when {
      vector != null -> vector!!.accept(visitor.vecType())
      number != null -> number!!.accept(visitor.numType())
      ref != null -> ref!!.accept(visitor.refType())
      abs != null -> visitor.refType(abs!!)
    }
  }
}
