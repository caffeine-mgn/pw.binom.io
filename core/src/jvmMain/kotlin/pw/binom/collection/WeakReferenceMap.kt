package pw.binom.collection

import java.util.*

actual class WeakReferenceMap<K : Any, V : Any> actual constructor() {
    private val native = WeakHashMap<K, V>()
    actual operator fun set(key: K, value: V) {
        native[key] = value
    }

    actual operator fun get(key: K): V? = native[key]
    actual fun delete(key: K) {
        native.remove(key)
    }

    actual operator fun contains(key: K): Boolean = key in native
}