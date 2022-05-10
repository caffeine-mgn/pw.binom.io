package pw.binom.collections

object EmptyIterator : MutableIterator<Any> {
    override fun hasNext(): Boolean = false

    override fun next(): Any {
        throw NoSuchElementException()
    }

    override fun remove() {
        throw NoSuchElementException()
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> emptyIterator() = EmptyIterator as MutableIterator<T>
