package pw.binom.collections

class SetBridge<E>(val map: Map<E, Boolean>) : Set<E> {
    override val size: Int
        get() = map.size

    override fun isEmpty(): Boolean = map.isEmpty()
    override fun iterator(): Iterator<E> = map.keys.iterator()
    override fun containsAll(elements: Collection<E>): Boolean = elements.all { contains(it) }
    override fun contains(element: E): Boolean = map.containsKey(element)
}
