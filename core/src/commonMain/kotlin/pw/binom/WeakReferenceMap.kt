package pw.binom

expect class WeakReferenceMap<K : Any, V : Any>() {
    operator fun set(key: K, value: V)
    operator fun get(key: K):V?
    actual operator fun contains(key: K): Boolean
    fun delete(key: K)
}