package pw.binom.collection

class EmptyIterator<T> : MutableIterator<T> {
    override fun hasNext(): Boolean = false

    override fun next(): T {
        throw NoSuchElementException()
    }

    override fun remove() {
        throw NoSuchElementException()
    }
}
