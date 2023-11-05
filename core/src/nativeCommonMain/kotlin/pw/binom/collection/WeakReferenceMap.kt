package pw.binom.collection

import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableMap
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalNativeApi::class)
actual class WeakReferenceMap<K : Any, V : Any> actual constructor() {
  val native = defaultMutableMap<WeakReference<K>, V>()

  private var cleanCounter = 0

  actual operator fun set(key: K, value: V) {
    cleanUp()
    native[WeakReference(key)] = value
  }

  actual operator fun get(key: K): V? {
    cleanUp()
    return native.entries.find { it.key.value === key }?.value
  }

  actual fun delete(key: K) {
    val ref = native.keys.find { it.value === key }
    native.remove(ref)
    cleanUp()
  }

  actual fun cleanUp(): Int {
    if (cleanCounter++ < 500) {
      return 0
    }
    var list: MutableList<WeakReference<K>>? = null
    native.keys.forEach {
      if (it.value == null) {
        if (list == null) {
          list = defaultMutableList()
        }
        list!!.add(it)
      }
    }
    list?.forEach {
      native.remove(it)
    }
    cleanCounter = 0
    return list?.size ?: 0
  }

  actual operator fun contains(key: K): Boolean = native.entries.any { it.key.value === key }
}
