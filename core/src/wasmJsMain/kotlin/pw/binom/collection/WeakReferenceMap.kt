package pw.binom.collection

@JsName("WeakMap")
external class JSWeakMap:JsAny {
  fun has(k: JsReference<Any>): Boolean
  fun set(k: JsReference<Any>, v: JsReference<Any>)
  fun get(k: JsReference<Any>): JsReference<Any>
  fun delete(k: JsReference<Any>)
}

actual class WeakReferenceMap<K : Any, V : Any> actual constructor() {
  private val native = JSWeakMap()
  actual operator fun set(key: K, value: V) {
    native.set(key.toJsReference(), value.toJsReference())
  }

  actual operator fun get(key: K): V? = native.get(key.toJsReference()) as? V?
  actual fun delete(key: K) {
    native.delete(key.toJsReference())
  }

  actual operator fun contains(key: K): Boolean = native.has(key.toJsReference())
  actual fun cleanUp(): Int = 0
}
