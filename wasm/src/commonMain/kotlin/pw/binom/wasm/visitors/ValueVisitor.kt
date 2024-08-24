package pw.binom.wasm.visitors

import pw.binom.wasm.AbsHeapType
import pw.binom.wasm.TypeId

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
