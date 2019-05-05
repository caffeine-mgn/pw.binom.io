package pw.binom

import pw.binom.atomic.AtomicInt
import pw.binom.atomic.AtomicReference

class FreezedStack<T> {
    val size: Int
        get() = _size.value

    fun clear() {
        lock.use {
            _size.value = 0
            top.value = null
            bottom.value = null
        }
    }

    private val lock = Lock()
    private val _size = AtomicInt(0)

    private inner class Item(val value: T, next: Item?, back: Item?) {
        val next = AtomicReference(next)
        val back = AtomicReference(back)
    }

    private val top = AtomicReference<Item?>(null)
    private val bottom = AtomicReference<Item?>(null)

    fun pushFirst(value: T) {
        lock.use {
            val i = Item(value, next = bottom.value, back = null).doFreeze()

            if (bottom.value == null)
                bottom.value = i

            top.value?.back?.value = i
            top.value = i
            _size.increment()
        }
    }

    fun pushLast(value: T) {
        lock.use {
            val i = Item(value, next = null, back = bottom.value).doFreeze()

            if (top.value == null)
                top.value = i

            bottom.value = i
            _size.increment()
        }
    }

    fun popFirst(): T = lock.use {
        val item = top.value ?: throw IllegalStateException("Stack is empty")

        top.value = item.next.value

        if (bottom.value == item)
            bottom.value = null
        _size.decrement()
        item.value
    }

    fun popLast(): T = lock.use {
        val item = bottom.value ?: throw IllegalStateException("Stack is empty")
        bottom.value = item.back.value

        if (top.value == item)
            top.value = null
        _size.decrement()
        item.value
    }

    fun peekFirst(): T {
        val item = top.value ?: throw NoSuchElementException()
        return item.value
    }

    fun peekLast(): T {
        val item = bottom.value ?: throw NoSuchElementException()
        return item.value
    }

    val isEmpty: Boolean
        get() = top.value == null

    fun asFiFoQueue() = object : AppendableQueue<T> {
        override val isEmpty: Boolean
            get() = this@FreezedStack.isEmpty

        override fun push(value: T) {
            pushFirst(value)
        }

        override fun pop(): T = popLast()

        override fun peek(): T = peekLast()

        init {
            doFreeze()
        }
    }

    fun asLiFoQueue() = object : AppendableQueue<T> {
        override val isEmpty: Boolean
            get() = this@FreezedStack.isEmpty

        override fun push(value: T) {
            pushFirst(value)
        }

        override fun pop(): T = popFirst()

        override fun peek(): T = peekFirst()

        init {
            doFreeze()
        }
    }

    init {
        doFreeze()
    }
}