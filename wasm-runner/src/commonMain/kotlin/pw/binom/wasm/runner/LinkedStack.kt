package pw.binom.wasm.runner

import pw.binom.collections.LinkedList

interface Stack {
  fun pushI32(value: Int)
  fun popI32(): Int
  fun peekI32(): Int

  fun pushI64(value: Long)
  fun popI64(): Long
  fun peekI64(): Long
}

class LinkedStack {
  private val l = LinkedList<Any>()
  val size
    get() = l.size

  fun push(value: Any) {
    l.addLast(value)
  }

  fun push(value: Int) {
    l.addLast(value)
  }

  fun push(value: Long) {
    l.addLast(value)
  }

  fun popI16(): Short {
    val value = l.removeLast()
    if (value !is Short) {
      TODO("$value is not short")
    }
    return value
  }

  fun popI32(): Int {
    val value = l.removeLast()
    if (value !is Int) {
      TODO("$value is not int")
    }
    return value
  }

  fun popI64(): Long {
    val value = l.removeLast()
    if (value !is Long) {
      TODO("$value is not int")
    }
    return value
  }

  fun pop() = l.removeLast()
  fun peek() = l.peekLast()!!

  fun clear(): List<Any> {
    val out = ArrayList<Any>()
    while (l.isNotEmpty()) {
      out += l.removeFirst()
    }
    return out
  }
}

