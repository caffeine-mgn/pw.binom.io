package pw.binom.collections

fun <K, V> defaultHashMap(): MutableMap<K, V> = HashMap2()
fun <K, V> defaultHashMap(capacity: Int) = defaultHashMap<K, V>()
fun <K, V> defaultHashMap(map: Map<K, V>): MutableMap<K, V> {
    val out = defaultHashMap<K, V>()
    out.putAll(map)
    if (map.isEmpty()) {
        return out
    }
    return out
}

fun <K> defaultHashSet(): MutableSet<K> = defaultHashMap<K, Boolean>().toBridgeSet()
fun <K> defaultHashSet(capacity: Int): MutableSet<K> =
    defaultHashMap<K, Boolean>(capacity).toBridgeSet() // defaultHashMap<K, Boolean>().toBridgeSet()

fun <K> defaultHashSet(set: Set<K>): MutableSet<K> {
    val out = defaultHashSet<K>()
    if (set.isEmpty()) {
        return out
    }
    out.addAll(set)
    return out
}
