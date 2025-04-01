package pw.binom.wasm.runner.stack

import pw.binom.wasm.node.ValueType
import pw.binom.wasm.runner.*

interface Stack {
  val size: Int
  fun push(value: Value)
  fun peek(): Value
  fun pop(): Value

  fun pushI32(value: Int) {
    push(Value.Primitive.I32(value))
  }

  fun popI32(): Int = (pop() as Value.Primitive.I32).value
  fun peekI32(): Int = (peek() as Value.Primitive.I32).value

  fun pushI64(value: Long) {
    push(Value.Primitive.I64(value))
  }

  fun popI64(): Long = (pop() as Value.Primitive.I64).value
  fun peekI64(): Long = (peek() as Value.Primitive.I64).value

  fun pushF32(value: Float) {
    push(Value.Primitive.F32(value))
  }

  fun popF32(): Float = (pop() as Value.Primitive.F32).value
  fun peekF32(): Float = (peek() as Value.Primitive.F32).value

  fun pushF64(value: Double) {
    push(Value.Primitive.F64(value))
  }

  fun popF64(): Double = (pop() as Value.Primitive.F64).value

  fun peekF64(): Double = (peek() as Value.Primitive.F64).value
  fun select() {
    val v = popI32()
    val v2 = pop()
    val v1 = pop()
    if (v1::class != v2::class) {
      TODO()
    }
    push(if (v != 0) v1 else v2)
  }
  fun drop() {
    pop()
  }
}
