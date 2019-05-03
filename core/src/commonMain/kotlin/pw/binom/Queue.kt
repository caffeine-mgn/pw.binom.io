package pw.binom

import pw.binom.atomic.AtomicReference

class Queue<T> {
    private val lock = Lock()

    private inner class Item(val value: T, next:Item?,back: Item?) {
        val next = AtomicReference(next)
        val back = AtomicReference(back)
    }

    private val top = AtomicReference<Item?>(null)
    private val bottom = AtomicReference<Item?>(null)

    fun pushFirst(value: T) {
        lock.use {
            val i = Item(value, next=bottom.value, back = null).doFreeze()

            if (bottom.value == null)
                bottom.value = i

            top.value?.back?.value = i
            top.value = i
        }
    }

    fun pushLast(value: T) {
        lock.use {
            val i = Item(value, next = null, back = bottom.value).doFreeze()

            if (top.value == null)
                top.value = i

            bottom.value = i
        }
    }

    fun popFirst(): T? = lock.use {
        val item = top.value

        top.value = item?.next?.value

        if (bottom.value == item)
            bottom.value = null

        item?.value
    }

    fun popLast(): T? = lock.use {
        val item = bottom.value
        bottom.value = item?.back?.value

        if (top.value == item)
            top.value = null

        item?.value
    }

    fun peekFirst(): T? = bottom.value?.value

    val isEmpty: Boolean
        get() = peekFirst() == null
}