package pw.binom.wasm.runner

import pw.binom.collections.LinkedList

class Stack {
  private val l = LinkedList<Any?>()
  val size
    get() = l.size

  fun push(value: Any?) {
    if (value == null) {
      TODO("value is null")
    }
    l.addLast(value)
  }

  fun push(value: Int) {
    l.addLast(value)
  }

  fun popI32() = l.removeLast() as Int
  fun pop() = l.removeLast()

  fun clear(): List<Any?> {
    val out = ArrayList<Any?>()
    while (l.isNotEmpty()) {
      out += l.removeFirst()
    }
    return out
  }
}

