package pw.binom.collections

class ArrayList2<T>(val initialCapacity: Int, loadFactor: Float = 1.75f) : RandomAccess, MutableList<T> {

    private var array = arrayOfUninitializedElements<T>(initialCapacity)

    override var size: Int = 0
        private set

    override fun contains(element: T): Boolean = indexOf(element) >= 0

    override fun containsAll(elements: Collection<T>): Boolean = elements.all { it in this }

    override fun get(index: Int): T {
        if (index < 0) {
            throw IndexOutOfBoundsException()
        }
        if (size == 0) {
            throw IndexOutOfBoundsException()
        }
        if (index >= size) {
            throw IndexOutOfBoundsException()
        }
        return array[index]
    }

    override fun indexOf(element: T): Int {
        for (i in 0 until size) {
            if (array[i] == element) {
                return i
            }
        }
        return -1
    }

    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): MutableIterator<T> {
        TODO("Not yet implemented")
    }

    override fun lastIndexOf(element: T): Int {
        TODO("Not yet implemented")
    }

    override fun add(element: T): Boolean {
        TODO("Not yet implemented")
    }

    override fun add(index: Int, element: T) {
        TODO("Not yet implemented")
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun listIterator(): MutableListIterator<T> {
        TODO("Not yet implemented")
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        TODO("Not yet implemented")
    }

    override fun remove(element: T): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAt(index: Int): T {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun set(index: Int, element: T): T {
        TODO("Not yet implemented")
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        TODO("Not yet implemented")
    }
}
