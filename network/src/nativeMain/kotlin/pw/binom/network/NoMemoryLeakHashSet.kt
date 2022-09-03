package pw.binom.network

class NoMemoryLeakHashSet<E> : MutableSet<E> {
    private var currentSet = HashSet<E>()

    private var coutner = 0
    private fun checkClean() {
        coutner++
        if (coutner > 10000) {
            coutner = 0
            val new = HashSet<E>()
            new.addAll(currentSet)
            currentSet.clear()
            currentSet = new
        }
    }

    override fun add(element: E): Boolean {
        checkClean()
        return currentSet.add(element)
    }

    override fun addAll(elements: Collection<E>): Boolean {
        checkClean()
        return currentSet.addAll(elements)
    }

    override val size: Int
        get() = currentSet.size

    override fun clear() {
        checkClean()
        currentSet.clear()
    }

    override fun isEmpty(): Boolean = currentSet.isEmpty()

    override fun containsAll(elements: Collection<E>): Boolean =
        currentSet.containsAll(elements)

    override fun contains(element: E): Boolean = currentSet.contains(element)

    override fun iterator(): MutableIterator<E> = currentSet.iterator()

    override fun retainAll(elements: Collection<E>): Boolean {
        checkClean()
        return currentSet.retainAll(elements)
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        checkClean()
        return currentSet.removeAll(elements)
    }

    override fun remove(element: E): Boolean {
        val result = currentSet.remove(element)
        checkClean()
        return result
    }
}
