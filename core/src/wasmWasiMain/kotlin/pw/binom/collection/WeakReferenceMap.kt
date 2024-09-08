package pw.binom.collection

actual class WeakReferenceMap<K : Any, V : Any> actual constructor() {
  actual operator fun set(key: K, value: V) {
    TODO()
  }

  actual operator fun get(key: K): V? = TODO()
  actual fun delete(key: K) {
    TODO()
  }

  actual operator fun contains(key: K): Boolean = TODO()
  actual fun cleanUp(): Int = 0
}
