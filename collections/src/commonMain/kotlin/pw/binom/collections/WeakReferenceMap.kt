package pw.binom.collections

class WeakReferenceMap<K : Any, V : Any> {
    private val native = HashMap2<WeakReference<K>, V>()

    private var cleanCounter = 0

    operator fun set(key: K, value: V) {
        delete(key)
        cleanUp()
        native[WeakReference(key)] = value
    }

    operator fun get(key: K): V? {
        cleanUp()
        return native.entries.find { it.key.get === key }?.value
    }

    fun delete(key: K) {
        val ref = native.keys.find { it.get === key }
        native.remove(ref)
        cleanUp()
    }

    fun forEach(func: (K, V) -> Unit) {
        native.forEach {
            val key = it.key.get
            if (key != null) {
                func(key, it.value)
            }
        }
    }

    fun cleanUp(): Int {
        if (cleanCounter++ < 500) {
            return 0
        }
        var list: MutableList<WeakReference<K>>? = null
        native.keys.forEach {
            if (it.get == null) {
                if (list == null) {
                    list = ArrayList2()
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

    operator fun contains(key: K): Boolean = native.entries.any { it.key.get === key }
}
