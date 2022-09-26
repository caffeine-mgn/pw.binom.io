package pw.binom.collections

class MutableSetBridge<E>(val map: MutableMap<E, Boolean>) : MutableSet<E> {
    override fun add(element: E): Boolean = map.put(element, true) == null

    override fun addAll(elements: Collection<E>): Boolean {
        var changed = false
        elements.forEach {
            if (add(it)) {
                changed = true
            }
        }
        return changed
    }

    override val size: Int
        get() = map.size

    override fun clear() {
        map.clear()
    }

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun containsAll(elements: Collection<E>): Boolean = elements.all { contains(it) }

    override fun contains(element: E): Boolean = map.containsKey(element)

    override fun iterator(): MutableIterator<E> = map.keys.iterator()

    override fun retainAll(elements: Collection<E>): Boolean {
        var changed = false
        val it = map.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (e.key !in elements) {
                it.remove()
                changed = true
            }
        }
        return changed
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var changed = false
        elements.forEach {
            if (remove(it)) {
                changed = true
            }
        }
        return changed
    }

    override fun remove(element: E): Boolean = map.remove(element) != null

    override fun toString(): String = map.toString()
}
