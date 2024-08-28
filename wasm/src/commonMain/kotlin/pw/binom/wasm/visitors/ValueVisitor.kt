package pw.binom.wasm.visitors

import pw.binom.wasm.AbsHeapType
import pw.binom.wasm.TypeId

interface ValueVisitor {
  companion object {
    val SKIP = object : ValueVisitor {}
  }

  interface HeapVisitor {
    companion object {
      val SKIP = object : HeapVisitor {}
    }

    fun type(type: AbsHeapType) {}
    fun type(type: TypeId) {}
  }

  interface NumberVisitor {
    companion object {
      val SKIP = object : NumberVisitor {}
    }

    fun i32() {}
    fun i64() {}
    fun f32() {}
    fun f64() {}
  }

  interface VectorVisitor {
    companion object {
      val SKIP = object : VectorVisitor {}
    }

    fun v128() {}
  }

  interface RefVisitor {
    companion object {
      val SKIP = object : RefVisitor {}
    }

    fun ref(): HeapVisitor = HeapVisitor.SKIP
    fun refNull(): HeapVisitor = HeapVisitor.SKIP
    fun refNull(type: AbsHeapType) {}
  }

  fun numType(): NumberVisitor = NumberVisitor.SKIP
  fun refType(): RefVisitor = RefVisitor.SKIP
  fun refType(type: AbsHeapType){}
  fun vecType(): VectorVisitor = VectorVisitor.SKIP
}
