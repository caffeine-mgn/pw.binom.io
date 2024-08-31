package pw.binom.wasm.dom

import pw.binom.wasm.Primitive
import pw.binom.wasm.visitors.ValueVisitor

class NumberType(var type: Primitive) : ValueVisitor.NumberVisitor {
  override fun i32() {
    type = Primitive.I32
  }

  override fun i64() {
    type = Primitive.I64
  }

  override fun f32() {
    type = Primitive.F32
  }

  override fun f64() {
    type = Primitive.F64
  }

  fun accept(visitor: ValueVisitor.NumberVisitor) {
    when (type) {
      Primitive.I32 -> visitor.i32()
      Primitive.I64 -> visitor.f64()
      Primitive.F32 -> visitor.f32()
      Primitive.F64 -> visitor.f64()
    }
  }
}
