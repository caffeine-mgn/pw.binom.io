package pw.binom


class Stack<T> {
    val size: Int
        get() = _size

    fun clear() {
        _size = 0
        top = null
        bottom = null
    }

    private var _size = 0

    private inner class Item(val value: T, var next: Item?, var back: Item?)

    private var top: Item? = null
    private var bottom: Item? = null

    fun pushFirst(value: T) {
        val i = Item(value, next = top, back = null)

        if (bottom == null)
            bottom = i

        top?.back = i
        top = i
        _size++
    }

    fun pushLast(value: T) {
        val i = Item(value, next = null, back = bottom)

        if (top == null)
            top = i

        bottom = i
        _size++
    }

    fun popFirst(result: PopResult<T>) {
        if (top == null) {
            result.clear()
            return
        }
        val item = top!!

        top = item.next

        if (bottom == item)
            bottom = null
        _size--
        result.set(item.value)
    }

    fun popFirst(): T {
        val item = top ?: throw IllegalStateException("Stack is empty")

        top = item.next

        if (bottom == item)
            bottom = null
        _size--
        return item.value
    }

    fun popLast(result: PopResult<T>) {
        if (bottom == null) {
            result.clear()
            return
        }
        val item = bottom!!
        bottom = item.back

        if (top == item)
            top = null
        _size--
        result.set(item.value)
    }

    fun popLast(): T {
        val item = bottom ?: throw IllegalStateException("Stack is empty")
        bottom = item.back

        if (top == item)
            top = null
        _size--
        return item.value
    }

    fun peekFirst(): T {
        val item = top ?: throw NoSuchElementException()
        return item.value
    }

    fun peekLast(): T {
        val item = bottom ?: throw NoSuchElementException()
        return item.value
    }

    val isEmpty: Boolean
        get() = top == null

    fun asFiFoQueue() = object : AppendableQueue<T> {
        override val size: Int
            get() = this@Stack.size

        override fun pop(dist: PopResult<T>) {
            if (isEmpty)
                dist.clear()
            else
                dist.set(pop())
        }

        override val isEmpty: Boolean
            get() = this@Stack.isEmpty

        override fun push(value: T) {
            pushFirst(value)
        }

        override fun pop(): T = popLast()

        override fun peek(): T = peekLast()

        init {
            neverFreeze()
        }
    }

    fun asLiFoQueue() = object : AppendableQueue<T> {
        override val size: Int
            get() = this@Stack.size

        override fun pop(dist: PopResult<T>) {
            if (isEmpty)
                dist.clear()
            else
                dist.set(pop())
        }

        override val isEmpty: Boolean
            get() = this@Stack.isEmpty

        override fun push(value: T) {
            pushFirst(value)
        }

        override fun pop(): T = popFirst()

        override fun peek(): T = peekFirst()

        init {
            neverFreeze()
        }
    }

    init {
        neverFreeze()
    }
}