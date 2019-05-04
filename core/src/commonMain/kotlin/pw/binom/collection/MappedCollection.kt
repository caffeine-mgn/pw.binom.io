package pw.binom.collection

class MappedCollection<E, R>(private val collection: Collection<E>, val mapper: (E) -> R) : Collection<R> {
    override val size: Int
        get() = collection.size

    override fun contains(element: R): Boolean =
            collection.asSequence().map(mapper).any { it == element }

    override fun containsAll(elements: Collection<R>): Boolean =
            collection.asSequence().map(mapper).count { it in elements } == elements.size

    override fun isEmpty(): Boolean = collection.isEmpty()

    override fun iterator(): Iterator<R> = MappedIterator(collection.iterator())

    private inner class MappedIterator(val it: Iterator<E>) : Iterator<R> {
        override fun hasNext(): Boolean = it.hasNext()

        override fun next(): R = mapper(it.next())

    }
}