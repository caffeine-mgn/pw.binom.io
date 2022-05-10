package pw.binom.collections

open class LinkedList<T>() : MutableList<T> {
    constructor(elements: Iterable<T>) : this() {
        addAll(elements)
    }

    constructor(elements: Array<out T>) : this() {
        addAll(elements)
    }

    protected class Node<T>(var prev: Node<T>?, var item: T?, var next: Node<T>?)

    private var first: Node<T>? = null
    private var last: Node<T>? = null

    private var _size = 0

    override val size: Int
        get() = _size

    private var modCount = 0

    override fun contains(element: T): Boolean = indexOf(element) >= 0

    override fun containsAll(elements: Collection<T>): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    override fun toString(): String = "[${joinToString(", ")}]"

    override fun get(index: Int): T {
        checkElementIndex(index)
        return node(index)!!.item as T
    }

    override fun indexOf(element: T): Int {
        var index = 0
        if (element == null) {
            var x: Node<T>? = first
            while (x != null) {
                if (x.item == null) return index
                index++
                x = x.next
            }
        } else {
            var x: Node<T>? = first
            while (x != null) {
                if (element == x.item) return index
                index++
                x = x.next
            }
        }
        return -1
    }

    override fun isEmpty(): Boolean = first == null

    override fun iterator(): MutableIterator<T> = listIterator()

    override fun lastIndexOf(element: T): Int {
        var index = size
        if (element == null) {
            var x: Node<T>? = last
            while (x != null) {
                index--
                if (x.item == null) return index
                x = x.prev
            }
        } else {
            var x: Node<T>? = last
            while (x != null) {
                index--
                if (element == x.item) return index
                x = x.prev
            }
        }
        return -1
    }

    protected fun linkLast(e: T) {
        val l = last
        val newNode = Node(l, e, null)
        last = newNode
        if (l == null) {
            first = newNode
        } else {
            l.next = newNode
        }
        _size++
        modCount++
    }

    override fun add(element: T): Boolean {
        linkLast(element)
        return true
    }

    private fun isPositionIndex(index: Int): Boolean = index >= 0 && index <= size

    private fun checkPositionIndex(index: Int) {
        if (!isPositionIndex(index))
            throw IndexOutOfBoundsException(outOfBoundsMsg(index))
    }

    private fun outOfBoundsMsg(index: Int) = "Index: $index, Size: $size"

    /**
     * Inserts element e before non-null Node succ.
     */
    protected fun linkBefore(e: T?, succ: Node<T>) {
        // assert succ != null;
        val pred = succ.prev
        val newNode = Node(pred, e, succ)
        succ.prev = newNode
        if (pred == null) {
            first = newNode
        } else {
            pred.next = newNode
        }
        _size++
        modCount++
    }

    /**
     * Returns the (non-null) Node at the specified element index.
     */
    protected fun node(index: Int): Node<T>? {
        // assert isElementIndex(index);
        return if (index < size shr 1) {
            var x: Node<T>? = first
            for (i in 0 until index) {
                x = x?.next
            }
            x
        } else {
            var x: Node<T>? = last
            for (i in size - 1 downTo index + 1) {
                x = x?.prev
            }
            x
        }
    }

    override fun add(index: Int, element: T) {
        checkPositionIndex(index)

        if (index == size)
            linkLast(element)
        else
            linkBefore(element, node(index)!!)
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        checkPositionIndex(index)
//        val a: Array<Any> = elements.toTypedArray() as Array<Any>
        val numNew = elements.size
        if (numNew == 0) return false

        var pred: Node<T>?
        val succ: Node<T>?
        if (index == size) {
            succ = null
            pred = last
        } else {
            succ = node(index)
            pred = succ!!.prev
        }

        for (o in elements) {
            val newNode = Node(pred, o, null)
            if (pred == null) first = newNode else pred.next = newNode
            pred = newNode
        }

        if (succ == null) {
            last = pred
        } else {
            pred!!.next = succ
            succ.prev = pred
        }

        _size += numNew
        modCount++
        return true
    }

    override fun addAll(elements: Collection<T>): Boolean = addAll(size, elements)

    override fun clear() {
        first = null
        last = null
        _size = 0
        modCount = 0
    }

    override fun listIterator(): MutableListIterator<T> = listIterator(0)

    override fun listIterator(index: Int): MutableListIterator<T> {
        checkPositionIndex(index)
        return ListItr(index)
    }

    override fun remove(element: T): Boolean {
        if (element == null) {
            var x: Node<T>? = first
            while (x != null) {
                if (x.item == null) {
                    unlink(x)
                    return true
                }
                x = x.next
            }
        } else {
            var x: Node<T>? = first
            while (x != null) {
                if (element == x.item) {
                    unlink(x)
                    return true
                }
                x = x.next
            }
        }
        return false
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (elements.contains(it.next())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }

    /**
     * Tells if the argument is the index of an existing element.
     */
    private fun isElementIndex(index: Int) = index >= 0 && index < size

    private fun checkElementIndex(index: Int) {
        if (!isElementIndex(index))
            throw IndexOutOfBoundsException(outOfBoundsMsg(index))
    }

    protected fun unlink(x: Node<T>): T {
        // assert x != null;
        val element = x.item
        val next = x.next
        val prev = x.prev

        if (prev == null) {
            first = next
        } else {
            prev.next = next
            x.prev = null
        }

        if (next == null) {
            last = prev
        } else {
            next.prev = prev
            x.next = null
        }

        x.item = null
        _size--
        modCount++
        return element as T
    }

    override fun removeAt(index: Int): T {
        checkElementIndex(index)
        return unlink(node(index)!!)
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun set(index: Int, element: T): T {
        checkElementIndex(index)
        val x = node(index)!!
        val oldVal = x.item
        x.item = element
        return oldVal as T
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        TODO("Not yet implemented")
    }

    /**
     * Inserts the specified element at the beginning of this list.
     * @param e the element to add
     */
    fun addFirst(e: T) {
        linkFirst(e)
    }

    /**
     * Returns the last element in this list.
     * @return Returns the last element in this list.
     * @throws NoSuchElementException – if this list is empty
     */
    fun getLast(): T {
        val l = last ?: throw NoSuchElementException()
        return l.item as T
    }

    /**
     * Appends the specified element to the end of this list.
     * This method is equivalent to [add].
     * @param e the element to add
     */
    fun addLast(e: T) {
        linkLast(e)
    }

    /**
     * Unlinks non-null first node f.
     */
    private fun unlinkFirst(f: Node<T>): T {
        // assert f == first && f != null;
        val element = f.item
        val next = f.next
        f.item = null
        f.next = null // help GC
        first = next
        if (next == null) {
            last = null
        } else {
            next.prev = null
        }
        _size--
        modCount++
        return element as T
    }

    private fun unlinkLast(l: Node<T>): T {
        // assert l == last && l != null;
        val element = l.item
        val prev = l.prev
        l.item = null
        l.prev = null // help GC
        last = prev
        if (prev == null)
            first = null
        else
            prev.next = null
        _size--
        modCount++
        return element as T
    }

    /**
     * Retrieves and removes the head (first element) of this list.
     * @return the head of this list, or null if this list is empty
     */
    fun poll(): T? {
        val f = first
        return if (f == null) null else unlinkFirst(f)
    }

    /**
     * Links e as first element.
     */
    private fun linkFirst(e: T) {
        val f = first
        val newNode = Node(null, e, f)
        first = newNode
        if (f == null) {
            last = newNode
        } else {
            f.prev = newNode
        }
        _size++
        modCount++
    }

    /**
     * Retrieves and removes the head (first element) of this list.
     * @return the head of this list
     * @throws NoSuchElementException – if this list is empty
     */
    fun remove() = removeFirst()

    /**
     * Retrieves, but does not remove, the first element of this list, or returns null if this list is empty.
     * @return the first element of this list, or null if this list is empty
     */
    fun peekFirst(): T? = first?.item

    /**
     * Retrieves, but does not remove, the head (first element) of this list.
     * @return the head of this list, or null if this list is empty
     */
    fun peek() = peekFirst()

    /**
     * Retrieves, but does not remove, the last element of this list, or returns null if this list is empty.
     * @return the last element of this list, or null if this list is empty
     */
    fun peekLast(): T? = last?.item

    /**
     * Retrieves and removes the first element of this list, or returns null if this list is empty.
     * @return the first element of this list, or null if this list is empty
     */
    fun pollFirst(): T? {
        val f = first
        return if (f == null) null else unlinkFirst(f)
    }

    /**
     * Retrieves and removes the last element of this list, or returns null if this list is empty.
     * @return the last element of this list, or null if this list is empty
     */
    fun pollLast(): T? {
        val l = last
        return if (l == null) null else unlinkLast(l)
    }

    /**
     * Pushes an element onto the stack represented by this list. In other words, inserts the element at the front of this list.
     * This method is equivalent to [addFirst].
     * @param e – the element to push
     */
    fun push(e: T) {
        addFirst(e)
    }

    /**
     * Pops an element from the stack represented by this list. In other words, removes and returns the first element of this list.
     * This method is equivalent to removeFirst().
     * @return the element at the front of this list (which is the top of the stack represented by this list)
     * @throws NoSuchElementException – if this list is empty
     */
    fun pop() = removeFirst()

    private inner class ListItr(index: Int) : MutableListIterator<T> {
        private var nextIndex = index
        private var expectedModCount = modCount
        private var next = if (index == size) null else node(index)
        private var lastReturned: Node<T>? = null
        override fun hasPrevious(): Boolean = nextIndex > 0

        override fun nextIndex(): Int = nextIndex

        override fun previous(): T {
            checkForComodification()
            if (!hasPrevious()) throw NoSuchElementException()

            lastReturned = if (next == null) last else next!!.prev.also { next = it }
            nextIndex--
            return lastReturned?.item as T
        }

        override fun previousIndex(): Int = nextIndex - 1

        override fun add(element: T) {
            checkForComodification()
            lastReturned = null
            if (next == null) {
                linkLast(element)
            } else {
                linkBefore(element, next!!)
            }
            nextIndex++
            expectedModCount++
        }

        override fun hasNext(): Boolean = nextIndex < size

        override fun next(): T {
            checkForComodification()
            if (!hasNext())
                throw NoSuchElementException()

            lastReturned = next
            next = next?.next
            nextIndex++
            return lastReturned?.item as T
        }

        override fun remove() {
            checkForComodification()
            if (lastReturned == null)
                throw IllegalStateException()

            val lastNext = lastReturned!!.next
            unlink(lastReturned!!)
            if (next == lastReturned)
                next = lastNext
            else
                nextIndex--
            lastReturned = null
            expectedModCount++
        }

        override fun set(element: T) {
            if (lastReturned == null)
                throw IllegalStateException()
            checkForComodification()
            lastReturned!!.item = element
        }

        fun checkForComodification() {
            if (modCount != expectedModCount) {
                throw ConcurrentModificationException()
            }
        }
    }
}

fun <T> linkedListOf(): LinkedList<T> = LinkedList()
fun <T> linkedListOf(vararg elements: T): LinkedList<T> = if (elements.isEmpty()) LinkedList() else LinkedList(elements)
