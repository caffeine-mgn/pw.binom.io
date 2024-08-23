package pw.binom.wasm.writers

import pw.binom.wasm.StreamWriter
import pw.binom.wasm.TypeId
import pw.binom.wasm.Types
import pw.binom.wasm.visitors.AbsHeapType
import pw.binom.wasm.visitors.ValueVisitor

class ValueWriter(private val out: StreamWriter) :
  ValueVisitor,
  ValueVisitor.RefVisitor,
  ValueVisitor.HeapVisitor,
  ValueVisitor.VectorVisitor,
  ValueVisitor.NumberVisitor {

  // ValueVisitor

  override fun vecType(): ValueVisitor.VectorVisitor = this
  override fun numType(): ValueVisitor.NumberVisitor = this

  override fun refType(): ValueVisitor.RefVisitor = this

  // ValueVisitor.RefVisitor

  override fun refNull(type: AbsHeapType) {
    type(type)
  }

  override fun ref(): ValueVisitor.HeapVisitor {
    out.write(0x64u)
    return this
  }

  override fun refNull(): ValueVisitor.HeapVisitor {
    out.write(0x64u)
    return this
  }

  // ValueVisitor.HeapVisitor

  override fun type(type: AbsHeapType) {
    val byte = when (type) {
      AbsHeapType.TYPE_REF_ABS_HEAP_NO_FUNC -> Types.TYPE_REF_ABS_HEAP_NO_FUNC
      AbsHeapType.TYPE_REF_ABS_HEAP_NO_EXTERN -> Types.TYPE_REF_ABS_HEAP_NO_EXTERN
      AbsHeapType.TYPE_REF_ABS_HEAP_NONE -> Types.TYPE_REF_ABS_HEAP_NONE
      AbsHeapType.TYPE_REF_ABS_HEAP_FUNC_REF -> Types.TYPE_REF_ABS_HEAP_FUNC_REF
      AbsHeapType.TYPE_REF_ABS_HEAP_EXTERN -> Types.TYPE_REF_ABS_HEAP_EXTERN
      AbsHeapType.TYPE_REF_ABS_HEAP_ANY -> Types.TYPE_REF_ABS_HEAP_ANY
      AbsHeapType.TYPE_REF_ABS_HEAP_EQ -> Types.TYPE_REF_ABS_HEAP_EQ
      AbsHeapType.TYPE_REF_ABS_HEAP_I31 -> Types.TYPE_REF_ABS_HEAP_I31
      AbsHeapType.TYPE_REF_ABS_HEAP_STRUCT -> Types.TYPE_REF_ABS_HEAP_STRUCT
      AbsHeapType.TYPE_REF_ABS_HEAP_ARRAY -> Types.TYPE_REF_ABS_HEAP_ARRAY
    }
    out.write(byte)
  }

  override fun type(type: TypeId) {
    out.v33s(type.value.toLong())
  }

  // ValueVisitor.VectorVisitor

  override fun v128() {
    out.write(Types.TYPE_VEC_V128)
  }


  // ValueVisitor.NumberVisitor

  override fun f32() {
    out.write(Types.TYPE_NUM_I32)
  }

  override fun f64() {
    out.write(Types.TYPE_NUM_F64)
  }

  override fun i32() {
    out.write(Types.TYPE_NUM_I32)
  }

  override fun i64() {
    out.write(Types.TYPE_NUM_I64)
  }
}
