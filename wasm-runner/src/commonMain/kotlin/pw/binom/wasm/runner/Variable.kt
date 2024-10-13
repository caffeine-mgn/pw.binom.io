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
      type.number?.type == Primitive.I64 -> I32(0)
      else -> TODO()
    }
  }

  val asI32
    get() = this as I32
  val asI64
    get() = this as I64

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
}
