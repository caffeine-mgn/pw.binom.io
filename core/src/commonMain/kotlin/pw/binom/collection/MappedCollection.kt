package pw.binom.collection

class MappedCollection<E, R>(private val collection: Collection<E>, val mapper: (E) -> R) : Collection<R> {
    override val size: Int
        get() = collection.size

    override fun contains(element: R): Boolean =
        collection.asSequence().map(mapper).any { it == element }

    override fun containsAll(elements: Collection<R>): Boolean =
        collection.asSequence().map(mapper).count { it in elements } == elements.size

    override fun isEmpty(): Boolean = collection.isEmpty()
    override fun iterator(): Iterator<R> = collection.iterator().mapped(mapper)
}

class MappedIterator<R, E>(val iterator: Iterator<E>, val mapper: (E) -> R) : Iterator<R> {
    override fun hasNext(): Boolean = iterator.hasNext()
    override fun next(): R = mapper(iterator.next())
}

fun <E, R> Iterator<E>.mapped(mapper: (E) -> R) = MappedIterator(iterator = this, mapper = mapper)
fun <E, R> Collection<E>.mapped(mapper: (E) -> R) = MappedCollection(this, mapper)
