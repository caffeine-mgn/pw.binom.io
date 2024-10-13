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

  fun pushF32(value: Float)
  fun popF32(): Float
  fun peekF32(): Float

  fun pushF64(value: Double)
  fun popF64(): Double
  fun peekF64(): Double

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

  override fun pushF32(value: Float) {
    push(value)
  }

  override fun popF32(): Float = pop() as Float
  override fun peekF32(): Float = peek() as Float

  override fun pushF64(value: Double) {
    push(value)
  }

  override fun popF64(): Double = pop() as Double
  override fun peekF64(): Double = peek() as Double

  override fun peekI32(): Int = when (val v = peek()) {
    is Int -> v
    is Long -> v.toInt()
    else -> TODO()
  }

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

