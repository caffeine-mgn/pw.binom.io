package pw.binom.collections

open class TreeSet<E> private constructor(private val backing: TreeMap<E, Boolean>) : NavigableSet<E>, MutableSet<E> {
    constructor() : this(TreeMap())
    constructor(comparator: Comparator<E>) : this(TreeMap<E, Boolean>(comparator))

    override fun add(element: E): Boolean = backing.put(element, true) == true

    override fun addAll(elements: Collection<E>): Boolean {
        var changed = false
        elements.forEach {
            if (add(it)) {
                changed = true
            }
        }
        return changed
    }

    override fun clear() {
        backing.clear()
    }

    override fun iterator(): MutableIterator<E> = backing.keys.iterator()

    override fun remove(element: E): Boolean = backing.remove(element) != null

    override fun removeAll(elements: Collection<E>): Boolean {
        var changed = false
        elements.forEach {
            if (remove(it)) {
                changed = true
            }
        }
        return changed
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        var changed = false
        val it = backing.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (e.key !in elements) {
                it.remove()
                changed = true
            }
        }
        return changed
    }

    override val size: Int
        get() = backing.size

    override fun contains(element: E): Boolean = backing.containsKey(element)

    override fun containsAll(elements: Collection<E>): Boolean = elements.all { backing.containsKey(it) }

    override fun isEmpty(): Boolean = backing.isEmpty()
}
