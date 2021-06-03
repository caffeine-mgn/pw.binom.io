package pw.binom

expect class WeakReferenceMap<K : Any, V : Any>() {
    operator fun set(key: K, value: V)
    operator fun get(key: K): V?
    actual operator fun contains(key: K): Boolean
    fun delete(key: K)
}

fun <K : Any, V : Any> WeakReferenceMap<K, V>.getOrDefault(key: K, default: () -> V): V =
    this[key] ?: default()

fun <K : Any, V : Any> WeakReferenceMap<K, V>.getOrPut(key: K, put: () -> V): V {
    val value = this[key]
    if (value == null) {
        val newValue = put()
        this[key] = newValue
        return newValue
    }
    return value
}