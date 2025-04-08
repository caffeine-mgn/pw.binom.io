package pw.binom.wasm.node

import pw.binom.wasm.AbsoluteHeapType
import pw.binom.wasm.Primitive
import pw.binom.wasm.Vector
import pw.binom.wasm.visitors.ValueVisitor

class ValueType : ValueVisitor {
  companion object {
    fun number(value: NumberType): ValueType {
      val r = ValueType()
      r.number = value
      return r
    }
    fun abs(value: AbsoluteHeapType): ValueType {
      val r = ValueType()
      r.abs = value
      return r
    }
    fun ref(value: RefType): ValueType {
      val r = ValueType()
      r.ref = value
      return r
    }
    fun vector(value: VectorType): ValueType {
      val r = ValueType()
      r.vector = value
      return r
    }
  }

  override fun toString(): String {
    number?.let { return it.toString() }
    abs?.let { return it.toString() }
    ref?.let { return it.toString() }
    vector?.let { return it.toString() }
    return super.toString()
  }

  var number: NumberType? = null
    set(value) {
      field = value
      if (value != null) {
        abs = null
        ref = null
        vector = null
      }
    }
  var abs: AbsoluteHeapType? = null
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

  override fun numType(): NumberType {
    val e = NumberType(Primitive.I32)
    number = e
    return e
  }

  override fun refType(): RefType {
    val e = RefType()
    ref = e
    return e
  }

  override fun refType(type: AbsoluteHeapType) {
    abs = type
  }

  override fun vecType(): VectorType {
    val e = VectorType(Vector.V128)
    vector = e
    return e
  }

  fun accept(visitor: ValueVisitor) {
    when {
      vector != null -> vector!!.accept(visitor.vecType())
      number != null -> number!!.accept(visitor.numType())
      ref != null -> ref!!.accept(visitor.refType())
      abs != null -> visitor.refType(abs!!)
      else -> throw IllegalStateException()
    }
  }
}
