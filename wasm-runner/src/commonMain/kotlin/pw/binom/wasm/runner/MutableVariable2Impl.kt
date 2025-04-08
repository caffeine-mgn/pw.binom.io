package pw.binom.wasm.runner

import pw.binom.wasm.runner.stack.Stack

interface Variable2 {
  val value: Value
  val type: VType
  fun pushToStack(stack: Stack) {
    stack.push(value)
  }
}

interface MutableValue2 : Variable2 {
  override var value: Value
  fun popFromStack(stack: Stack) {
    value = stack.pop()
  }

  fun peekToStack(stack: Stack) {
    value = stack.peek()
  }
}

data class VariableImpl(override val value: Value, override val type: VType) : Variable2

data class MutableVariable2Impl(override var value: Value, override val type: VType) : MutableValue2
