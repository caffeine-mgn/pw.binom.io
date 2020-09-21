package pw.binom

@JsName("WeakMap")
external class JSWeakMap {
    fun has(k: dynamic): Boolean
    fun set(k: dynamic, v: dynamic)
    fun get(k: dynamic): dynamic
    fun delete(k: dynamic)
}