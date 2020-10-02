package pw.binom

actual class WeakReferenceMap<K : Any, V : Any> actual constructor() {
    private val native = JSWeakMap()
    actual operator fun set(key: K, value: V) {
        native.set(key, value)
    }

    actual operator fun get(key: K): V? = native.get(key) as? V?
    actual fun delete(key: K) {
        native.delete(key)
    }

    actual operator fun contains(key: K): Boolean = native.has(key)
}