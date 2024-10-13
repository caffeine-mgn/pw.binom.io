package pw.binom.wasm.runner

import pw.binom.wasm.Primitive
import pw.binom.wasm.node.ValueType

sealed interface Variable {
  fun popFromStack(stack: Stack)
  fun pushToStack(stack: Stack)
  fun peekToStack(stack: Stack)

  companion object {
    fun create(type: ValueType) = when {
      type.number?.type == Primitive.I32 -> I32(0)
      type.number?.type == Primitive.I64 -> I64(0)
      type.number?.type == Primitive.F32 -> F32(0f)
      type.number?.type == Primitive.F64 -> F64(0.0)
      else -> TODO()
    }
  }

  val asI32
    get() = this as I32
  val asI64
    get() = this as I64
  val asF32
    get() = this as F32
  val asF64
    get() = this as F64

  class I32(var value: Int) : Variable {

    override fun popFromStack(stack: Stack) {
      value = stack.popI32()
    }

    override fun pushToStack(stack: Stack) {
      stack.pushI32(value)
    }

    override fun peekToStack(stack: Stack) {
      value = stack.peekI32()
    }
  }

  class I64(var value: Long) : Variable {

    override fun popFromStack(stack: Stack) {
      value = stack.popI64()
    }

    override fun pushToStack(stack: Stack) {
      stack.pushI64(value)
    }

    override fun peekToStack(stack: Stack) {
      value = stack.peekI64()
    }
  }

  class F32(var value: Float) : Variable {
    override fun popFromStack(stack: Stack) {
      value = stack.popF32()
    }

    override fun pushToStack(stack: Stack) {
      stack.pushF32(value)
    }

    override fun peekToStack(stack: Stack) {
      value = stack.peekF32()
    }
  }

  class F64(var value: Double) : Variable {
    override fun popFromStack(stack: Stack) {
      value = stack.popF64()
    }

    override fun pushToStack(stack: Stack) {
      stack.pushF64(value)
    }

    override fun peekToStack(stack: Stack) {
      value = stack.peekF64()
    }
  }
}
