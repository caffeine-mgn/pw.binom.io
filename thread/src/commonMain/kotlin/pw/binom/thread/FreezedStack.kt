package pw.binom.thread

import pw.binom.AppendableQueue
import pw.binom.PopResult
import pw.binom.atomic.AtomicInt
import pw.binom.atomic.AtomicReference
import pw.binom.collection.EmptyIterator
import pw.binom.doFreeze
import pw.binom.io.hold

class FreezedStack<T> : MutableIterable<T> {
    private var changeCounter = AtomicInt(0)

    private inner class TopBottomStackIterator : MutableIterator<T> {
        var changeCounter = this@FreezedStack.changeCounter.value
        private var first = true

        init {
            check(top.value != null)
        }

        var currentItem: Item? = null
        override fun hasNext(): Boolean = lock.hold {
            if (changeCounter != this@FreezedStack.changeCounter.value)
                throw ConcurrentModificationException()
            if (currentItem == null && first && !isEmpty)
                return true
            return currentItem?.next?.value != null
        }

        override fun next(): T =
                lock.hold {
                    if (changeCounter != this@FreezedStack.changeCounter.value)
                        throw ConcurrentModificationException()
                    if (currentItem == null && first && !isEmpty) {
                        currentItem = this@FreezedStack.top.value
                        first = false
                        return currentItem!!.value
                    }

                    if (currentItem?.next?.value == null)
                        throw NoSuchElementException()
                    val next = currentItem!!.next.value ?: throw NoSuchElementException()
                    currentItem = next
                    next.value
                }

        override fun remove() {
            if (currentItem == null)
                throw NoSuchElementException()
            this@FreezedStack.remove(currentItem!!)
            this@FreezedStack.changeCounter.increment()
            changeCounter = this@FreezedStack.changeCounter.value
            currentItem = currentItem?.back?.value
            _size.decrement()
        }

    }

    val size: Int
        get() = _size.value

    fun clear() {
        lock.hold {
            _size.value = 0
            top.value = null
            bottom.value = null
            changeCounter.increment()
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
        lock.hold {
            val i = Item(value, next = bottom.value, back = null).doFreeze()

            if (bottom.value == null)
                bottom.value = i

            top.value?.back?.value = i
            top.value = i
            _size.increment()
            changeCounter.increment()
        }
    }

    fun pushLast(value: T) {
        lock.hold {
            val i = Item(value, next = null, back = bottom.value).doFreeze()

            if (top.value == null)
                top.value = i

            bottom.value?.next?.value = i
            bottom.value = i
            _size.increment()
            changeCounter.increment()
        }
    }

    private fun remove(item: Item) {
        if (top.value == item) {
            top.value = item.next.value
        }

        if (bottom.value == item) {
            bottom.value = item.back.value
        }
        item.back.value?.let {
            it.next.value = item.next.value
        }

        item.next.value?.let {
            it.back.value = item.back.value
        }

    }

    private fun privatePopFirst(): T {
        val item = top.value ?: throw IllegalStateException("Stack is empty")

        top.value = item.next.value

        if (bottom.value == item)
            bottom.value = null
        _size.decrement()
        changeCounter.increment()
        return item.value
    }

    fun popFirst(): T = lock.hold {
        privatePopFirst()
    }

    private fun privatePopLast(): T {
        val item = bottom.value ?: throw IllegalStateException("Stack is empty")
        bottom.value = item.back.value

        if (top.value == item)
            top.value = null
        _size.decrement()
        changeCounter.increment()
        return item.value
    }

    fun popLast(): T = lock.hold {
        privatePopLast()
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
        override val size: Int
            get() = this@FreezedStack.size

        override fun pop(dist: PopResult<T>) {
            lock.hold {
                if (isEmpty)
                    dist.clear()
                else
                    dist.set(privatePopLast())
            }
        }

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
        override val size: Int
            get() = this@FreezedStack.size

        override fun pop(dist: PopResult<T>) {
            lock.hold {
                if (isEmpty)
                    dist.clear()
                else
                    dist.set(privatePopFirst())
            }
        }

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

    override fun iterator(): MutableIterator<T> {
        if (isEmpty) return EmptyIterator()
        return TopBottomStackIterator()
    }
}