package pw.binom.wasm.runner

import pw.binom.collections.LinkedList

class Stack {
  private val l = LinkedList<Any>()
  val size
    get() = l.size

  fun push(value: Any) {
    if (value == null) {
      TODO("value is null")
    }
    l.addLast(value)
  }

  fun push(value: Int) {
    l.addLast(value)
  }

  fun push(value: Long) {
    l.addLast(value)
  }

  fun popI32():Int{
    val value = l.removeLast()
    if (value !is Int){
      TODO("$value is not int")
    }
    return value
  }
  fun popI64():Long{
    val value = l.removeLast()
    if (value !is Long){
      TODO("$value is not int")
    }
    return value
  }
  fun pop() = l.removeLast()

  fun clear(): List<Any> {
    val out = ArrayList<Any>()
    while (l.isNotEmpty()) {
      out += l.removeFirst()
    }
    return out
  }
}

