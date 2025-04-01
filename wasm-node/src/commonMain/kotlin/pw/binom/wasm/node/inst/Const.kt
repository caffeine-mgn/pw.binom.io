package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

sealed class Const : Inst(){
  data class I32Const(var value: Int) : Const() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.const(value)
    }

    override fun toString(): String = "i32.const $value"
  }
  data class I64Const(var value: Long) : Const() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.const(value)
    }

    override fun toString(): String = "i64.const $value"
  }
  data class F32Const(var value: Float) : Const() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.const(value)
    }
    override fun toString(): String = "f32.const $value"
  }
  data class F64Const(var value: Double) : Const() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.const(value)
    }
    override fun toString(): String = "f64.const $value"
  }
}
