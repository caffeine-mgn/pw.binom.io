package pw.binom.utils

interface NavigableMap<K, V> : SortedMap<K, V> {
    /**
     * Returns a key-value mapping associated with the least key in this map, or null if the map is empty.
     */
    val firstEntry: Map.Entry<K, V>?

    /**
     * Returns a key-value mapping associated with the greatest key in this map, or null if the map is empty.
     */
    val lastEntry: Map.Entry<K, V>?

    /**
     * Removes and returns a key-value mapping associated with the least key in this map, or null if the map is empty.
     */
    fun pollFirstEntry(): Map.Entry<K, V>?

    /**
     * Removes and returns a key-value mapping associated with the greatest key in this map, or null if the map is empty.
     */
    fun pollLastEntry(): Map.Entry<K, V>?

    /**
     * Returns a key-value mapping associated with the greatest key strictly less than the given key, or null
     * if there is no such key.
     * @param key the key
     */
    fun lowerEntry(key: K): Map.Entry<K, V>?

    /**
     * Returns a key-value mapping associated with the greatest key less than or equal to the given key, or null
     * if there is no such key.
     * @param key the key
     */
    fun floorEntry(key: K): Map.Entry<K, V>?

    /**
     * Returns a key-value mapping associated with the least key greater than or equal to the given key, or null
     * if there is no such key.
     * @param key the key
     */
    fun ceilingEntry(key: K): Map.Entry<K, V>?

    /**
     * Returns a key-value mapping associated with the least key strictly greater than the given key, or null
     * if there is no such key.
     * @param key the key
     */
    fun higherEntry(key: K): Map.Entry<K, V>?
}
