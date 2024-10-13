package pw.binom.wasm.runner

import pw.binom.collections.LinkedList
import pw.binom.wasm.node.ValueType

interface Stack {
  fun pushI32(value: Int)
  fun popI32(): Int
  fun peekI32(): Int

  fun pushI64(value: Long)
  fun popI64(): Long
  fun peekI64(): Long

  fun pop(type: ValueType): Variable {
    val v = Variable.create(type)
    v.popFromStack(this)
    return v
  }
}

class LinkedStack : Stack {
  private val l = LinkedList<Any>()
  val size
    get() = l.size

  fun push(value: Any) {
    l.addLast(value)
  }

  override fun pushI32(value: Int) {
    l.addLast(value)
  }

  override fun pushI64(value: Long) {
    l.addLast(value)
  }

  override fun peekI32(): Int = peek() as Int
  override fun peekI64(): Long = peek() as Long

  override fun popI32(): Int {
    val value = l.removeLast()
    if (value !is Int) {
      TODO("$value is not int")
    }
    return value
  }

  override fun popI64(): Long {
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

