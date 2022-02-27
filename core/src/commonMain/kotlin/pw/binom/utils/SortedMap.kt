package pw.binom.utils

interface SortedMap<K, V> : Map<K, V> {
    /**
     * Returns the first (lowest) key currently in this map.
     * @throws NoSuchElementException – if this map is empty
     */
    val firstKey: K?

    /**
     * Returns the last (highest) key currently in this map.
     * @throws NoSuchElementException – if this map is empty
     */
    val lastKey: K?
}
