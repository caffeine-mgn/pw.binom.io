package pw.binom.collections

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class ArrayList2<E> private constructor(
    private var backingArray: Array<E>,
    private var offset: Int,
    private var length: Int,
    private var isReadOnly: Boolean,
    private val backingList: ArrayList2<E>?,
    private val root: ArrayList2<E>?
) : MutableList<E>, RandomAccess, AbstractMutableList<E>(), BinomCollection {

    private fun log(txt: String) {
//        println("ArrayList2: $txt")
    }

    constructor() : this(10)

    constructor(initialCapacity: Int) : this(
        arrayOfUninitializedElements(initialCapacity),
        0,
        0,
        false,
        null,
        null
    )

    constructor(elements: Collection<E>) : this(elements.size) {
        addAll(elements)
    }

    private val constructTime = TimeSource.Monotonic.markNow()

    override val liveTime: Duration
        get() = constructTime.elapsedNow()
    override var name: String = ""

    @PublishedApi
    internal fun build(): List<E> {
        if (backingList != null) throw IllegalStateException() // just in case somebody casts subList to ArrayList
        checkIsMutable()
        isReadOnly = true
        return this
    }

    override val size: Int
        get() = length

    override fun isEmpty(): Boolean = length == 0

    val capacity
        get() = backingArray.size

    override fun get(index: Int): E {
        log("get($index)")
        checkElementIndex(index)
        return backingArray[offset + index]
    }

    private fun checkTrim() {
        if (!USE_TRIM_LIST) {
            return
        }
        val factor = 0.7f
        // capacity=100
        // size=30
        // capacity*factor=20
        // 50/100 = 0.5
        // 20/100=0.2
        // 70/100=0.7
        if (size.toFloat() / capacity.toFloat() < factor) {
            trimToSize()
        }
    }

    override operator fun set(index: Int, element: E): E {
        log("set($index, $element)")
        checkIsMutable()
        checkElementIndex(index)
        val old = backingArray[offset + index]
        backingArray[offset + index] = element
        return old
    }

    override fun indexOf(element: E): Int {
        log("indexOf($element)")
        var i = 0
        while (i < length) {
            if (backingArray[offset + i] == element) return i
            i++
        }
        return -1
    }

    override fun lastIndexOf(element: E): Int {
        log("lastIndexOf($element)")
        var i = length - 1
        while (i >= 0) {
            if (backingArray[offset + i] == element) return i
            i--
        }
        return -1
    }

    override fun iterator(): MutableIterator<E> = Itr(this, 0)
    override fun listIterator(): MutableListIterator<E> = Itr(this, 0)

    override fun listIterator(index: Int): MutableListIterator<E> {
        checkPositionIndex(index)
        return Itr(this, index)
    }

    override fun add(element: E): Boolean {
        log("add($element)")
        checkIsMutable()
        addAtInternal(offset + length, element)
        return true
    }

    override fun add(index: Int, element: E) {
        log("add($index, $element)")
        checkIsMutable()
        checkPositionIndex(index)
        addAtInternal(offset + index, element)
    }

    override fun addAll(elements: Collection<E>): Boolean {
        log("addAll($elements)")
        checkIsMutable()
        val n = elements.size
        addAllInternal(offset + length, elements, n)
        return n > 0
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        log("addAll($index, $elements)")
        checkIsMutable()
        checkPositionIndex(index)
        val n = elements.size
        addAllInternal(offset + index, elements, n)
        return n > 0
    }

    override fun clear() {
        log("clear()")
        checkIsMutable()
        removeRangeInternal(offset, length)
    }

    override fun removeAt(index: Int): E {
        log("removeAt($index)")
        checkIsMutable()
        checkElementIndex(index)
        return removeAtInternal(offset + index)
    }

    override fun remove(element: E): Boolean {
        log("remove($element)")
        checkIsMutable()
        val i = indexOf(element)
        if (i >= 0) removeAt(i)
        return i >= 0
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        log("removeAll($elements)")
        checkIsMutable()
        return retainOrRemoveAllInternal(offset, length, elements, false) > 0
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        log("retainAll($elements)")
        checkIsMutable()
        return retainOrRemoveAllInternal(offset, length, elements, true) > 0
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        log("subList($fromIndex, $toIndex)")
        checkRangeIndexes(fromIndex, toIndex)
        return ArrayList2(backingArray, offset + fromIndex, toIndex - fromIndex, isReadOnly, this, root ?: this)
    }

    fun trimToSize() {
        log("trimToSize()")
        if (backingList != null) throw IllegalStateException() // just in case somebody casts subList to ArrayList
        if (length < backingArray.size) {
            backingArray = backingArray.copyOfUninitializedElements(length)
        }
    }

    private val maxArraySize = Int.MAX_VALUE - 8
    internal fun newCapacity(oldCapacity: Int, minCapacity: Int): Int {
        // overflow-conscious
        var newCapacity = oldCapacity + (oldCapacity shr 1)
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity
        }
        if (newCapacity - maxArraySize > 0) {
            newCapacity = if (minCapacity > maxArraySize) Int.MAX_VALUE else maxArraySize
        }
        return newCapacity
    }

    final fun ensureCapacity(minCapacity: Int) {
        if (backingList != null) throw IllegalStateException() // just in case somebody casts subList to ArrayList
        if (minCapacity < 0) throw IllegalArgumentException("minCapacity<0") // overflow
        if (minCapacity > backingArray.size) {
            val newSize = newCapacity(backingArray.size, minCapacity)
            backingArray = backingArray.copyOfUninitializedElements(newSize)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other === this ||
            (other is List<*>) && contentEquals(other)
    }

    override fun hashCode(): Int {
        return backingArray.subarrayContentHashCode(offset, length)
    }

    override fun toString(): String {
        return backingArray.subarrayContentToStringImpl(offset, length)
    }
    /*
        @Suppress("UNCHECKED_CAST")
        override fun <T> toArray(array: Array<T>): Array<T> {
            if (array.size < length) {
                return backingArray.copyOfRange(fromIndex = offset, toIndex = offset + length) as Array<T>
            }

            (backingArray as Array<T>).copyInto(array, 0, startIndex = offset, endIndex = offset + length)

            if (array.size > length) {
                array[length] = null as T // null-terminate
            }

            return array
        }

        override fun toArray(): Array<Any?> {
            @Suppress("UNCHECKED_CAST")
            return backingArray.copyOfRange(fromIndex = offset, toIndex = offset + length) as Array<Any?>
        }
    */
    // ---------------------------- private ----------------------------

    private fun checkElementIndex(index: Int) {
        if (index < 0 || index >= length) {
            throw IndexOutOfBoundsException("index: $index, size: $length")
        }
    }

    private fun checkPositionIndex(index: Int) {
        if (index < 0 || index > length) {
            throw IndexOutOfBoundsException("index: $index, size: $length")
        }
    }

    private fun checkRangeIndexes(fromIndex: Int, toIndex: Int) {
        if (fromIndex < 0 || toIndex > length) {
            throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex, size: $length")
        }
        if (fromIndex > toIndex) {
            throw IllegalArgumentException("fromIndex: $fromIndex > toIndex: $toIndex")
        }
    }

    private fun checkIsMutable() {
        if (isReadOnly || root != null && root.isReadOnly) throw UnsupportedOperationException()
    }

    private fun ensureExtraCapacity(n: Int) {
        ensureCapacity(length + n)
    }

    private fun contentEquals(other: List<*>): Boolean {
        return backingArray.subarrayContentEquals(offset, length, other)
    }

    private fun insertAtInternal(i: Int, n: Int) {
        ensureExtraCapacity(n)
        backingArray.copyInto(backingArray, startIndex = i, endIndex = offset + length, destinationOffset = i + n)
        length += n
    }

    private fun addAtInternal(i: Int, element: E) {
        if (backingList != null) {
            backingList.addAtInternal(i, element)
            backingArray = backingList.backingArray
            length++
        } else {
            insertAtInternal(i, 1)
            backingArray[i] = element
        }
    }

    private fun addAllInternal(i: Int, elements: Collection<E>, n: Int) {
        if (backingList != null) {
            backingList.addAllInternal(i, elements, n)
            backingArray = backingList.backingArray
            length += n
        } else {
            insertAtInternal(i, n)
            var j = 0
            val it = elements.iterator()
            while (j < n) {
                backingArray[i + j] = it.next()
                j++
            }
        }
    }

    private fun removeAtInternal(i: Int): E {
        if (backingList != null) {
            val old = backingList.removeAtInternal(i)
            length--
            return old
        } else {
            val old = backingArray[i]
            backingArray.copyInto(backingArray, startIndex = i + 1, endIndex = offset + length, destinationOffset = i)
            backingArray.resetAt(offset + length - 1)
            length--
            checkTrim()
            return old
        }
    }

    private fun removeRangeInternal(rangeOffset: Int, rangeLength: Int) {
        if (backingList != null) {
            backingList.removeRangeInternal(rangeOffset, rangeLength)
        } else {
            backingArray.copyInto(
                backingArray,
                startIndex = rangeOffset + rangeLength,
                endIndex = length,
                destinationOffset = rangeOffset
            )
            backingArray.resetRange(fromIndex = length - rangeLength, toIndex = length)
        }
        length -= rangeLength
    }

    /** Retains elements if [retain] == true and removes them it [retain] == false. */
    private fun retainOrRemoveAllInternal(
        rangeOffset: Int,
        rangeLength: Int,
        elements: Collection<E>,
        retain: Boolean
    ): Int {
        if (backingList != null) {
            val removed = backingList.retainOrRemoveAllInternal(rangeOffset, rangeLength, elements, retain)
            length -= removed
            return removed
        } else {
            var i = 0
            var j = 0
            while (i < rangeLength) {
                if (elements.contains(backingArray[rangeOffset + i]) == retain) {
                    backingArray[rangeOffset + j++] = backingArray[rangeOffset + i++]
                } else {
                    i++
                }
            }
            val removed = rangeLength - j
            backingArray.copyInto(
                backingArray,
                startIndex = rangeOffset + rangeLength,
                endIndex = length,
                destinationOffset = rangeOffset + j
            )
            backingArray.resetRange(fromIndex = length - removed, toIndex = length)
            length -= removed
            checkTrim()
            return removed
        }
    }

    private class Itr<E> : MutableListIterator<E> {
        private val list: ArrayList2<E>
        private var index: Int
        private var lastIndex: Int

        constructor(list: ArrayList2<E>, index: Int) {
            this.list = list
            this.index = index
            this.lastIndex = -1
        }

        override fun hasPrevious(): Boolean = index > 0
        override fun hasNext(): Boolean = index < list.length

        override fun previousIndex(): Int = index - 1
        override fun nextIndex(): Int = index

        override fun previous(): E {
            if (index <= 0) throw NoSuchElementException()
            lastIndex = --index
            return list.backingArray[list.offset + lastIndex]
        }

        override fun next(): E {
            if (index >= list.length) throw NoSuchElementException()
            lastIndex = index++
            return list.backingArray[list.offset + lastIndex]
        }

        override fun set(element: E) {
            check(lastIndex != -1) { "Call next() or previous() before replacing element from the iterator." }
            list.set(lastIndex, element)
        }

        override fun add(element: E) {
            list.add(index++, element)
            lastIndex = -1
        }

        override fun remove() {
            check(lastIndex != -1) { "Call next() or previous() before removing element from the iterator." }
            list.removeAt(lastIndex)
            index = lastIndex
            lastIndex = -1
        }
    }
}

private fun <T> Array<T>.subarrayContentHashCode(offset: Int, length: Int): Int {
    var result = 1
    var i = 0
    while (i < length) {
        val nextElement = this[offset + i]
        result = result * 31 + nextElement.hashCode()
        i++
    }
    return result
}

private fun <T> Array<T>.subarrayContentEquals(offset: Int, length: Int, other: List<*>): Boolean {
    if (length != other.size) return false
    var i = 0
    while (i < length) {
        if (this[offset + i] != other[i]) return false
        i++
    }
    return true
}

internal inline fun <T> Array<out T>.subarrayContentToStringImpl(offset: Int, length: Int): String {
    val sb = StringBuilder(2 + length * 3)
    sb.append("[")
    var i = 0
    while (i < length) {
        if (i > 0) sb.append(", ")
        sb.append(this[offset + i])
        i++
    }
    sb.append("]")
    return sb.toString()
}
