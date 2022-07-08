package pw.binom.collection

import kotlin.native.ref.WeakReference

actual class WeakReferenceMap<K : Any, V : Any> actual constructor() {
    val native = HashMap<WeakReference<K>, V>()

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

    private fun cleanUp() {
        if (cleanCounter++ < 500) {
            return
        }
        var list: ArrayList<WeakReference<K>>? = null
        native.keys.forEach {
            if (it.value == null) {
                if (list == null)
                    list = ArrayList()
                list!!.add(it)
            }
        }
        list?.forEach {
            native.remove(it)
        }
        cleanCounter = 0
    }

    actual operator fun contains(key: K): Boolean = native.entries.any { it.key.value === key }
}
