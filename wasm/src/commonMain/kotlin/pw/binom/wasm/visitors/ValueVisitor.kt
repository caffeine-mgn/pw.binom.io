package pw.binom.wasm.visitors

import pw.binom.wasm.TypeId

enum class AbsHeapType {
  TYPE_REF_ABS_HEAP_NO_FUNC,
  TYPE_REF_ABS_HEAP_NO_EXTERN,
  TYPE_REF_ABS_HEAP_NONE,
  TYPE_REF_ABS_HEAP_FUNC_REF,
  TYPE_REF_ABS_HEAP_EXTERN,
  TYPE_REF_ABS_HEAP_ANY,
  TYPE_REF_ABS_HEAP_EQ,
  TYPE_REF_ABS_HEAP_I31,
  TYPE_REF_ABS_HEAP_STRUCT,
  TYPE_REF_ABS_HEAP_ARRAY,
}

interface ValueVisitor {
  companion object {
    val SKIP = object : ValueVisitor {}
  }

  interface HeapVisitor {
    companion object {
      val EMPTY = object : HeapVisitor {}
    }

    fun type(type: AbsHeapType) {}
    fun type(type: TypeId) {}
  }

  interface NumberVisitor {
    companion object {
      val EMPTY = object : NumberVisitor {}
    }

    fun i32() {}
    fun i64() {}
    fun f32() {}
    fun f64() {}
  }

  interface VectorVisitor {
    companion object {
      val EMPTY = object : VectorVisitor {}
    }

    fun v128() {}
  }

  interface RefVisitor {
    companion object {
      val EMPTY = object : RefVisitor {}
    }

    fun ref(): HeapVisitor = HeapVisitor.EMPTY
    fun refNull(): HeapVisitor = HeapVisitor.EMPTY
    fun refNull(type: AbsHeapType) {}
  }

  fun numType(): NumberVisitor = NumberVisitor.EMPTY
  fun refType(): RefVisitor = RefVisitor.EMPTY
  fun refType(type: AbsHeapType){}
  fun vecType(): VectorVisitor = VectorVisitor.EMPTY
}
