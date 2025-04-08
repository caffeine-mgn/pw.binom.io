package pw.binom.wasm.runner.stack

import pw.binom.wasm.runner.Value

class BaseStack : Stack {
  private val values = ArrayList<Value>()
  override val size: Int
    get() = values.size

  override fun push(value: Value) {
//    println("  PUSH $value")
    values += value
  }

  override fun peek(): Value{
    val e = values.last()
//    println("  PEEK $e")
    return e
  }

  override fun pop(): Value{
    val e = values.removeLast()
//    println("  POP $e")
    return e
  }
}
