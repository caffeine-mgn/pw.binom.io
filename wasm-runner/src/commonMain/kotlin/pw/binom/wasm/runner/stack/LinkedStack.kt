package pw.binom.wasm.runner.stack
/*
import pw.binom.collections.LinkedList
import pw.binom.wasm.runner.FunRef
import pw.binom.wasm.runner.Ref

class LinkedStack : AbstractStack() {
  private enum class Type {
    I32,
    I64,
    F32,
    F64,
    FUN_REF,
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
    val v2 = popAny()
    val v1 = popAny()
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

  override fun pushFunRef(value: FunRef) {
    q.addLast(value)
    types.addLast(Type.FUN_REF)
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

  override fun popFunRef(): FunRef {
    TODO("Not yet implemented")
  }

  override fun peekFunRef(): FunRef {
    TODO("Not yet implemented")
  }

  override fun pushRef(value: Ref) {
    TODO("Not yet implemented")
  }

  override fun popRef(): Ref {
    TODO("Not yet implemented")
  }

  override fun peekRef(): Ref {
    TODO("Not yet implemented")
  }

  override fun peekI32(): Int = q.peekLast()!! as Int
  override fun peekI64(): Long = q.peekLast()!! as Long
  override fun peekF32(): Float = q.peekLast()!! as Float

  override fun peekF64(): Double = q.peekLast()!! as Double

  override fun popAny(): Any {
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

*/
