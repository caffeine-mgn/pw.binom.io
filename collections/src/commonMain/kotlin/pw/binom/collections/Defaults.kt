package pw.binom.collections

fun <T> defaultMutableList() = ArrayList<T>().autoTrimmed()
fun <T> defaultMutableList(list: Collection<T>) = ArrayList(list).autoTrimmed()
fun <T> defaultMutableList(capacity: Int) = ArrayList<T>(capacity).autoTrimmed().also { it.ensureCapacity(capacity) }
fun <K, V> defaultMutableMap(): MutableMap<K, V> = HashMap2()
fun <K, V> defaultMutableMap(capacity: Int) = defaultMutableMap<K, V>()
fun <K, V> defaultMutableMap(map: Map<K, V>): MutableMap<K, V> {
    val out = defaultMutableMap<K, V>()
    out.putAll(map)
    if (map.isEmpty()) {
        return out
    }
    return out
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
