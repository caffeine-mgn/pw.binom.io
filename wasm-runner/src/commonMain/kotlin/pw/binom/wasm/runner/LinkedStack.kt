package pw.binom.wasm.runner

import pw.binom.collections.LinkedList
import pw.binom.wasm.node.ValueType

class ArrayStack(initSize: Int = 30) : Stack {
  companion object {
    const val I32: Byte = 0
    const val I64: Byte = 1
    const val F32: Byte = 2
    const val F64: Byte = 3
  }

  private var dataSize = 0
  private var typeSize = 0
  private val data = IntArray(initSize)
  private val types = ByteArray(initSize)

  override val size: Int
    get() = typeSize

  override fun pushI32(value: Int) {
    pushInt(value)
    pushType(I32)
  }

  override fun pushI64(value: Long) {
    pushLong(value)
    pushType(I64)
  }

  override fun pushF32(value: Float) {
    pushInt(value.toRawBits())
    pushType(F32)
  }

  override fun pushF64(value: Double) {
    pushLong(value.toRawBits())
    pushType(F64)
  }

  override fun popI32(): Int {
    check(popType() == I32)
    return popInt()
  }

  override fun popI64(): Long {
    check(popType() == I64)
    return popLong()
  }

  override fun popF32(): Float {
    check(popType() == F32)
    return Float.fromBits(popInt())
  }

  override fun popF64(): Double {
    check(popType() == F64)
    return Double.fromBits(popLong())
  }

  override fun peekI32(): Int {
    check(peekType() == I32)
    return peekInt()
  }

  override fun peekI64(): Long {
    check(peekType() == I64)
    return peekLong()
  }

  override fun peekF32(): Float {
    check(peekType() == F32)
    return Float.fromBits(peekInt())
  }

  override fun peekF64(): Double {
    check(peekType() == F64)
    return Double.fromBits(peekLong())
  }

  private fun pop(): Any {
    val type = peekType()
    return when (type) {
      I32 -> popI32()
      I64 -> popI64()
      F32 -> popF32()
      F64 -> popF64()
      else -> TODO()
    }
  }

  private fun push(value: Any) {
    when (value) {
      is Int -> pushI32(value)
      is Long -> pushI64(value)
      is Float -> pushF32(value)
      is Double -> pushF64(value)
      else -> TODO()
    }
  }

  override fun select() {
    val v = popI32()
    val v2 = pop()
    val v1 = pop()
    if (v1::class != v2::class) {
      TODO()
    }
    push(if (v != 0) v1 else v2)
  }

  override fun drop() {
    when (popType()) {
      F64 -> popInt()
      I64 -> popInt()
    }
    popInt()
  }

  private fun peekInt(back: Int = 0): Int {
    if (dataSize <= back) {
      TODO()
    }
    return data[dataSize - (1 + back)]
  }

  private fun peekLong(): Long {
    if (dataSize <= 1) {
      TODO()
    }

    val left = peekInt(0)
    val right = peekInt(1)
    return Long.fromComponent(left, right)
  }

  private fun pushInt(value: Int) {
    if (dataSize + 1 > data.size) {
      TODO()
    }
    data[dataSize++] = value
  }

  private fun popInt(): Int {
    if (dataSize - 1 < 0) {
      TODO()
    }
    return data[--dataSize]
  }

  private fun pushType(type: Byte) {
    if (typeSize + 1 > types.size) {
      TODO()
    }
    types[typeSize++] = type
  }

  private fun popType(): Byte {
    if (typeSize - 1 < 0) {
      TODO()
    }
    return types[--typeSize]
  }

  private fun peekType(): Byte {
    if (typeSize <= 0) {
      TODO()
    }
    return types[typeSize - 1]
  }

  private fun pushLong(value: Long) {
    pushInt(value.right)
    pushInt(value.left)
  }

  private fun popLong(): Long {
    val left = popInt()
    val right = popInt()
    return Long.fromComponent(left, right)
  }

  private fun Long.Companion.fromComponent(a: Int, b: Int): Long = a.toLong() shl 32 or b.toLong()
  private val Long.left
    get() = ushr(32).toInt()
  private val Long.right
    get() = (this and UInt.MAX_VALUE.toLong()).toInt()
}

interface Stack {
  val size: Int
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
  fun select()
  fun drop()

  fun pop(type: ValueType): Variable {
    val v = Variable.create(type)
    v.popFromStack(this)
    return v
  }
}

class LinkedStack : Stack {
  private enum class Type {
    I32,
    I64,
    F32,
    F64,
  }

  private val q = LinkedList<Any>()
  private val types = LinkedList<Type>()
  override val size
    get() = q.size

  private fun push(value: Any) {
    when (value) {
      is Int -> pushI32(value)
      is Long -> pushI64(value)
      is Float -> pushF32(value)
      is Double -> pushF64(value)
      else -> TODO()
    }
  }

  override fun select() {
    val v = popI32()
    val v2 = pop()
    val v1 = pop()
    if (v1::class != v2::class) {
      TODO()
    }
    push(if (v != 0) v1 else v2)
  }

  override fun pushI32(value: Int) {
    q.addLast(value)
    types.addLast(Type.I32)
  }

  override fun pushI64(value: Long) {
    q.addLast(value)
    types.addLast(Type.I64)
  }

  override fun pushF32(value: Float) {
    q.addLast(value)
    types.addLast(Type.F32)
  }

  override fun pushF64(value: Double) {
    q.addLast(value)
    types.addLast(Type.F64)
  }

  override fun popI32(): Int {
    check(types.removeLast() == Type.I32)
    return q.removeLast() as Int
  }

  override fun popI64(): Long {
    check(types.removeLast() == Type.I64)
    return q.removeLast() as Long
  }

  override fun popF32(): Float {
    check(types.removeLast() == Type.F32)
    return q.removeLast() as Float
  }

  override fun popF64(): Double {
    check(types.removeLast() == Type.F64)
    return q.removeLast() as Double
  }

  override fun peekI32(): Int = q.peekLast()!! as Int
  override fun peekI64(): Long = q.peekLast()!! as Long
  override fun peekF32(): Float = q.peekLast()!! as Float

  override fun peekF64(): Double = q.peekLast()!! as Double

  private fun pop(): Any {
    types.removeLast()
    return q.removeLast()
  }

  override fun drop() {
    types.removeLast()
    q.removeLast()
  }

  fun peek() = q.peekLast()!!

  fun clear(): List<Any> {
    val out = ArrayList<Any>()
    while (q.isNotEmpty()) {
      types.removeFirst()
      out += q.removeFirst()
    }
    return out
  }
}

