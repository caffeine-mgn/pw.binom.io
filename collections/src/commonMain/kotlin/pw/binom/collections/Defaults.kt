package pw.binom.collections

import pw.binom.atomic.AtomicBoolean

object LiveCollections {
    private val lock = AtomicBoolean(false)
    private val aliveCollection = WeakReferenceMap<BinomCollection, String>()

    private fun lock() {
        while (true) {
            if (lock.compareAndSet(false, true)) {
                break
            }
        }
    }

    internal fun <T : BinomCollection> reg(collection: T): T {
        lock()
        try {
            aliveCollection[collection] = Throwable().stackTraceToString()
        } finally {
            lock.setValue(false)
        }
        return collection
    }

    fun getAllAlive(): List<Pair<BinomCollection, String>> {
        val result = ArrayList2<Pair<BinomCollection, String>>()
        lock()
        try {
            aliveCollection.cleanUp()
            aliveCollection.forEach { collection, stack ->
                result += collection to stack
            }
        } finally {
            lock.setValue(false)
        }
        return result
    }
}

// ------------------------------------//
// fun <T> defaultMutableList(): MutableList<T> = LiveCollections.reg(ArrayList2())

fun <T> defaultMutableList(list: Collection<T>): MutableList<T> = LiveCollections.reg(ArrayList2(list))
fun <T> defaultMutableList(capacity: Int): MutableList<T> = LiveCollections.reg(ArrayList2(capacity))
fun <T> defaultMutableList(): MutableList<T> = ArrayList()
// fun <T> defaultMutableList(list: Collection<T>): MutableList<T> = ArrayList(list)
// fun <T> defaultMutableList(capacity: Int): MutableList<T> = ArrayList(capacity)

// ------------------------------------//
fun <K, V> defaultMutableMap() = LiveCollections.reg(HashMap2<K, V>())
fun <K, V> defaultMutableMap(capacity: Int) = defaultMutableMap<K, V>()
fun <K, V> defaultMutableMap(map: Map<K, V>) = defaultMutableMap<K, V>().also {
    if (map.isEmpty()) {
        return it
    }
    it.putAll(map)
}

fun <K> defaultMutableSet(): MutableSet<K> = defaultMutableMap<K, Boolean>().toBridgeSet()
fun <K> defaultMutableSet(capacity: Int): MutableSet<K> =
    defaultMutableMap<K, Boolean>(capacity).toBridgeSet() // defaultHashMap<K, Boolean>().toBridgeSet()

fun <K> defaultMutableSet(set: Set<K>): MutableSet<K> {
    val out = defaultMutableSet<K>()
    if (set.isEmpty()) {
        return out
    }
    out.addAll(set)
    return out
}
