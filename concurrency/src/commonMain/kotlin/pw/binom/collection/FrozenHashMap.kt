package pw.binom.collection

import pw.binom.atomic.AtomicInt
import pw.binom.atomic.AtomicReference
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.math.absoluteValue

class FrozenHashMap<K, V>(bucketSize: Int = 16) : MutableMap<K, V> {
    init {
        require(bucketSize >= 1)
    }

    private var internalSize = AtomicInt(0)
    override val size: Int
        get() = internalSize.getValue()

    private val entitySet = FrozenEntitySet(this)
    private val keySet = FrozenKeysSet(this)

    internal val buckets = Array(bucketSize) {
        FrozenBucket<K, V>()
    }

    override fun containsKey(key: K): Boolean {
        require(key != null)
        return getEntity(key) != null
    }

    override fun containsValue(value: V): Boolean =
        buckets.any { it.findValue(value) != null }

    override fun get(key: K): V? =
        getEntity(key)?.value

    private fun getEntity(key: K): FrozenMutableEntry<K, V>? {
        require(key != null)
        val bucket = calcBucket(key)
        return buckets[bucket].findKey(key)
    }

    private fun calcBucket(key: K) =
        if (key == null) -1 else key.hashCode().absoluteValue % buckets.size

    override fun isEmpty(): Boolean = size == 0

    override val entries
        get() = entitySet

    override val keys: MutableSet<K>
        get() = keySet

    override val values: MutableCollection<V>
        get() = TODO("Not yet implemented")

    override fun clear() {
        buckets.forEach {
            it.clear()
        }
    }

    internal fun internalPut(key: K, value: V, modify: (() -> Unit)?): V? {
        require(key != null)
        val bucket = buckets[calcBucket(key)]
        return bucket.put(
            key = key,
            value = value,
            modify = modify,
            new = { internalSize.increment() },
        )
    }

    override fun put(key: K, value: V): V? =
        internalPut(key, value, null)

    override fun putAll(from: Map<out K, V>) {
        from.forEach {
            put(it.key, it.value)
        }
    }

    internal fun internalRemove(key: K, modify: (() -> Unit)?): V? {
        require(key != null)
        return buckets[calcBucket(key)].remove(key) {
            internalSize.decrement()
            modify?.invoke()
        }
    }

    override fun remove(key: K): V? =
        internalRemove(key, null)
}

class FrozenEntityIterator<K, V>(val map: FrozenHashMap<K, V>) : MutableIterator<MutableMap.MutableEntry<K, V>> {
    private var bucketIndex = 0
    private var currentBucket = map.buckets[bucketIndex]
    private var currentChangeCount = currentBucket.changeCounter.getValue()
    private var frozenMutableEntry: FrozenMutableEntry<K, V>? = map.buckets[0].root.getValue()
    private fun hasNext(skipChangeCountCheck: Boolean): Boolean {
        if (frozenMutableEntry != null) {
            if (!skipChangeCountCheck && currentChangeCount != currentBucket.changeCounter.getValue()) {
                throw ConcurrentModificationException()
            }
            return true
        }
        frozenMutableEntry = frozenMutableEntry?.nextValue?.getValue()
        if (frozenMutableEntry != null) {
            if (!skipChangeCountCheck && currentChangeCount != currentBucket.changeCounter.getValue()) {
                throw ConcurrentModificationException()
            }
            return true
        }
        while (true) {
            if (bucketIndex >= map.buckets.lastIndex) {
                return false
            }
            bucketIndex++
            currentBucket = map.buckets[bucketIndex]
            currentChangeCount = currentBucket.changeCounter.getValue()
            frozenMutableEntry = currentBucket.root.getValue()
            if (frozenMutableEntry != null) {
                return true
            }
        }
    }

    override fun next(): MutableMap.MutableEntry<K, V> {
        if (!hasNext()) {
            throw NoSuchElementException()
        }
        return frozenMutableEntry!!
    }

    override fun remove() {
        if (!hasNext()) {
            throw NoSuchElementException()
        }
        currentBucket.remove(frozenMutableEntry!!.key, null)
        hasNext(true)
    }

    override fun hasNext(): Boolean = hasNext(false)
}

class FrozenEntitySet<K, V>(val map: FrozenHashMap<K, V>) : MutableSet<MutableMap.MutableEntry<K, V>> {
    override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
        TODO("Not yet implemented")
    }

    override fun clear() {
        map.clear()
    }

    override fun iterator() =
        FrozenEntityIterator(map)

    override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = TODO("Not yet implemented")

    override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean = map.isEmpty()
}

class FrozenKeyIterator<K, V>(val entityIterator: FrozenEntityIterator<K, V>) : MutableIterator<K> {
    override fun hasNext(): Boolean =
        entityIterator.hasNext()

    override fun next(): K =
        entityIterator.next().key

    override fun remove() {
        entityIterator.remove()
    }
}

class FrozenKeysSet<K, V>(val map: FrozenHashMap<K, V>) : MutableSet<K> {
    override fun add(element: K): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<K>): Boolean {
        TODO()
    }

    override fun clear() {
        map.clear()
    }

    override fun iterator(): MutableIterator<K> =
        FrozenKeyIterator(map.entries.iterator())

    override fun remove(element: K): Boolean {
        var changed = false
        map.internalRemove(element) { changed = true }
        return changed
    }

    override fun removeAll(elements: Collection<K>): Boolean {
        var any = false
        elements.forEach {
            if (remove(it)) {
                any = true
            }
        }
        return any
    }

    override fun retainAll(elements: Collection<K>): Boolean {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = map.size

    override fun contains(element: K): Boolean =
        map.containsKey(element)

    override fun containsAll(elements: Collection<K>): Boolean =
        elements.all { map.containsKey(it) }

    override fun isEmpty(): Boolean = map.isEmpty()
}

class FrozenBucket<K, V> {
    val lock = SpinLock("FrozenBucket")
    var root = AtomicReference<FrozenMutableEntry<K, V>?>(null)
    internal var changeCounter = AtomicInt(0)

    private fun findKey2(key: K): Pair<FrozenMutableEntry<K, V>?, FrozenMutableEntry<K, V>>? {
        val hashCode = key.hashCode()
        var p: FrozenMutableEntry<K, V>? = null
        var c = root.getValue()
        while (c != null) {
            if (c.key.hashCode() != hashCode) {
                p = c
                c = c.nextValue.getValue()
                continue
            }
            if (c.key!! != key) {
                p = c
                c = c.nextValue.getValue()
                continue
            }
            return p to c
        }
        return null
    }

    fun findKey(key: K): FrozenMutableEntry<K, V>? =
        lock.synchronize {
            findKey2(key)?.second
        }

    fun findValue(value: V): FrozenMutableEntry<K, V>? {
        lock.synchronize {
            var c = root.getValue()
            while (c != null) {
                if (c.value != value) {
                    c = c.nextValue.getValue()
                    continue
                }
                return c
            }
        }
        return null
    }

    fun put(key: K, value: V, modify: (() -> Unit)?, new: (() -> Unit)?): V? =
        lock.synchronize {
            val old = findKey2(key)?.second
            if (old != null) {
                val v = old.setValue(value)
                if (v != value) {
                    modify?.invoke()
                    changeCounter.increment()
                }
                v
            } else {
                modify?.invoke()
                new?.invoke()
                changeCounter.increment()
                val e = FrozenMutableEntry(key, value)
                e.nextValue.setValue(root.getValue())
                root.setValue(e)
                null
            }
        }

    fun remove(key: K, modify: (() -> Unit)?): V? {
        lock.synchronize {
            val en = findKey2(key) ?: return null
            en.first?.nextValue?.setValue(en.second.nextValue.getValue())
            changeCounter.increment()
            modify?.invoke()
            return en.second.value
        }
    }

    fun clear() {
        lock.synchronize {
            changeCounter.increment()
            root.setValue(null)
        }
    }
}

class FrozenMutableEntry<K, V>(key: K, value: V) : MutableMap.MutableEntry<K, V> {
    private val _key = AtomicReference(key)
    private val _value = AtomicReference(value)

    override val key
        get() = _key.getValue()
    override val value
        get() = _value.getValue()
    internal val nextValue = AtomicReference<FrozenMutableEntry<K, V>?>(null)

    override fun setValue(newValue: V): V {
        val oldValue = _value.getValue()
        _value.setValue(newValue)
        return oldValue
    }
}
