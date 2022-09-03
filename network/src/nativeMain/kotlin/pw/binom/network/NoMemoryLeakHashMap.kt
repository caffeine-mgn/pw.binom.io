package pw.binom.network

/**
 * Workaround for native memory leak in [HashMap]
 * Task [KT-53310](https://youtrack.jetbrains.com/issue/KT-53310)
 */
class NoMemoryLeakHashMap<K, V> : MutableMap<K, V> {

    private var coutner = 0
    private fun checkClean() {
        coutner++
        if (coutner > 10000) {
            coutner = 0
            val new = HashMap<K, V>()
            new.putAll(currentMap)
            currentMap.clear()
            currentMap = new
        }
    }

    private var currentMap = HashMap<K, V>()
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = currentMap.entries
    override val keys: MutableSet<K>
        get() = currentMap.keys
    override val size: Int
        get() = currentMap.size
    override val values: MutableCollection<V>
        get() = currentMap.values

    override fun clear() {
        currentMap.clear()
        checkClean()
    }

    override fun isEmpty(): Boolean = currentMap.isEmpty()

    override fun remove(key: K): V? {
        val result = currentMap.remove(key)
        checkClean()
        return result
    }

    override fun putAll(from: Map<out K, V>) {
        checkClean()
        currentMap.putAll(from)
    }

    override fun put(key: K, value: V): V? {
        checkClean()
        return currentMap.put(key, value)
    }

    override fun get(key: K): V? {
        return currentMap[key]
    }

    override fun containsValue(value: V): Boolean =
        currentMap.containsValue(value)

    override fun containsKey(key: K): Boolean =
        currentMap.containsKey(key)
}
