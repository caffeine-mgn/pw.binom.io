package pw.binom.collections

import pw.binom.atomic.AtomicInt
import kotlin.math.absoluteValue
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private val DEFAULT_LOAD_FACTOR = 0.75f
private val DEFAULT_CAPACITY = 11

private object NullKey

@OptIn(ExperimentalTime::class)
class HashMap2<K, V>(bucketSize: Int = DEFAULT_CAPACITY, val loadFactor: Float = DEFAULT_LOAD_FACTOR) :
    MutableMap<K, V>, BinomCollection {
    init {
        require(bucketSize >= 1)
    }

    private val constructTime = TimeSource.Monotonic.markNow()

    override val liveTime: Duration
        get() = constructTime.elapsedNow()
    override var name: String = ""

    private var threshold = (bucketSize * loadFactor).toInt()

    private var internalSize = AtomicInt(0)
    override val size: Int
        get() = internalSize.getValue()

    private val entitySet by lazy { EntitySet(this) }
    private val keySet by lazy { KeysSet(this) }
    private val valueSet by lazy { ValueSet(this) }

    internal var buckets = Array(bucketSize) {
        Bucket<K, V>()
    }

    override fun containsKey(key: K): Boolean = getEntity(key) != null

    override fun containsValue(value: V): Boolean = buckets.any { it.findValue(value) != null }

    override fun get(key: K): V? = getEntity(key)?.value

    @Suppress("UNCHECKED_CAST")
    private fun getEntity(key: K): MutableEntry<K, V>? {
        val notNullKey = key ?: NullKey as K
        val bucket = hash(notNullKey)
        return buckets[bucket].findKey(notNullKey)
    }

    private fun hash(key: K) = if (key == null) -1 else (key.hashCode() % buckets.size).absoluteValue

    override fun isEmpty(): Boolean = size == 0

    fun rehach(bucketCount: Int) {
        val oldBucket = buckets
        buckets = Array(bucketCount) { Bucket() }
        oldBucket.forEach { buckets ->
            buckets.forEach {
                put(it.key, it.value)
                true
            }
        }
    }

    override val entries
        get() = entitySet

    override val keys: MutableSet<K>
        get() = keySet

    override val values: MutableCollection<V>
        get() = valueSet

    override fun clear() {
        buckets.forEach {
            it.clear()
        }
        internalSize.setValue(0)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun internalPut(key: K, value: V, modify: (() -> Unit)?): V? {
        val notNullKey = key ?: (NullKey as K)
        val bucket = buckets[hash(notNullKey)]
        return bucket.put(
            key = notNullKey,
            value = value,
            modify = modify,
            new = { internalSize.increment() },
        )
    }

    override fun put(key: K, value: V): V? = internalPut(
        key = key,
        value = value,
        modify = null,
    )

    override fun putAll(from: Map<out K, V>) {
        from.forEach {
            put(it.key, it.value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun internalRemove(key: K, modify: (() -> Unit)?): V? {
        val notNullKey = key ?: (NullKey as K)
        return buckets[hash(notNullKey)].remove(notNullKey) {
            internalSize.decrement()
            modify?.invoke()
        }
    }

    override fun remove(key: K): V? = internalRemove(key, null)

    override fun toString(): String {
        val sb = StringBuilder()
        buckets.forEachIndexed { index, bucket ->
            if (index > 0) {
                sb.append("\n")
            }
            sb.append("Bucket $index: $bucket")
        }

        return sb.toString()
    }
}

class EntityIterator<K, V>(val map: HashMap2<K, V>) : MutableIterator<MutableMap.MutableEntry<K, V>> {
    private var bucketIndex = 0
    private var currentBucket = map.buckets[bucketIndex]
    private var currentChangeCount = currentBucket.changeCounter.getValue()
    private var frozenMutableEntry: MutableEntry<K, V>? = map.buckets[0].root
    private var ready = frozenMutableEntry != null
    private fun hasNext(skipChangeCountCheck: Boolean): Boolean {
        if (frozenMutableEntry != null && ready) {
            if (!skipChangeCountCheck && currentChangeCount != currentBucket.changeCounter.getValue()) {
                throw ConcurrentModificationException()
            }
            return true
        }
        frozenMutableEntry = frozenMutableEntry?.nextValue
        if (frozenMutableEntry != null) {
            if (!skipChangeCountCheck && currentChangeCount != currentBucket.changeCounter.getValue()) {
                throw ConcurrentModificationException()
            }
            ready = true
            return true
        }
        while (true) {
            if (bucketIndex >= map.buckets.lastIndex) {
                return false
            }
            bucketIndex++
            currentBucket = map.buckets[bucketIndex]
            currentChangeCount = currentBucket.changeCounter.getValue()
            frozenMutableEntry = currentBucket.root
            if (frozenMutableEntry != null) {
                ready = true
                return true
            }
        }
    }

    override fun next(): MutableMap.MutableEntry<K, V> {
        if (!hasNext()) {
            throw NoSuchElementException()
        }
        val r = frozenMutableEntry!!
//        frozenMutableEntry = null
        ready = false
        return r
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

class EntitySet<K, V>(val map: HashMap2<K, V>) : MutableSet<MutableMap.MutableEntry<K, V>> {
    override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun clear() {
        map.clear()
    }

    override fun iterator() = EntityIterator(map)

    override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean = throw UnsupportedOperationException()

    override fun removeAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean =
        throw UnsupportedOperationException()

    override fun retainAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean =
        throw UnsupportedOperationException()

    override val size: Int
        get() = map.size

    override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean = map.containsKey(element.key)

    override fun containsAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean =
        elements.all { contains(it) }

    override fun isEmpty(): Boolean = map.isEmpty()
}

class KeyIterator<K, V>(val entityIterator: EntityIterator<K, V>) : MutableIterator<K> {
    override fun hasNext(): Boolean = entityIterator.hasNext()

    override fun next(): K = entityIterator.next().key

    override fun remove() {
        entityIterator.remove()
    }
}

class ValueIterator<K, V>(val entityIterator: EntityIterator<K, V>) : MutableIterator<V> {
    override fun hasNext(): Boolean = entityIterator.hasNext()

    override fun next(): V = entityIterator.next().value

    override fun remove() {
        entityIterator.remove()
    }
}

class ValueSet<K, V>(val map: HashMap2<K, V>) : MutableSet<V> {
    override fun add(element: V): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<V>): Boolean {
        TODO()
    }

    override fun clear() {
        map.clear()
    }

    override fun iterator(): MutableIterator<V> = ValueIterator(map.entries.iterator())

    override fun remove(element: V): Boolean {
        map.buckets.forEach {
            if (it.removeValue(element, null)) {
                return true
            }
        }
        return false
    }

    override fun removeAll(elements: Collection<V>): Boolean {
        var any = false
        elements.forEach {
            if (remove(it)) {
                any = true
            }
        }
        return any
    }

    override fun retainAll(elements: Collection<V>): Boolean {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = map.size

    override fun contains(element: V): Boolean = map.containsValue(element)

    override fun containsAll(elements: Collection<V>): Boolean = elements.all { map.containsValue(it) }

    override fun isEmpty(): Boolean = map.isEmpty()
}

class KeysSet<K, V>(val map: HashMap2<K, V>) : MutableSet<K> {
    override fun add(element: K): Boolean {
        throw UnsupportedOperationException()
    }

    override fun addAll(elements: Collection<K>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun clear() {
        map.clear()
    }

    override fun iterator(): MutableIterator<K> = KeyIterator(map.entries.iterator())

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

    override fun contains(element: K): Boolean = map.containsKey(element)

    override fun containsAll(elements: Collection<K>): Boolean = elements.all { map.containsKey(it) }

    override fun isEmpty(): Boolean = map.isEmpty()
}

class Bucket<K, V> {
    var root: MutableEntry<K, V>? = null
    var size: Int = 0
        private set
    internal var changeCounter = AtomicInt(0)

    fun forEach(func: (MutableEntry<K, V>) -> Boolean) {
        var c = root
        while (c != null) {
            if (!func(c)) {
                return
            }
            c = c.nextValue
        }
    }

    private fun findKey2(key: K): Pair<MutableEntry<K, V>?, MutableEntry<K, V>>? {
        val hashCode = key.hashCode()
        var p: MutableEntry<K, V>? = null
        var c = root
        while (c != null) {
            if (c.key.hashCode() != hashCode) {
                p = c
                c = c.nextValue
                continue
            }
            if (c.key!! != key) {
                p = c
                c = c.nextValue
                continue
            }
            return p to c
        }
        return null
    }

    fun findKey(key: K): MutableEntry<K, V>? = findKey2(key)?.second

    fun findValue(value: V): Pair<MutableEntry<K, V>?, MutableEntry<K, V>>? {
        val hashCode = value.hashCode()
        var p: MutableEntry<K, V>? = null
        var c = root
        while (c != null) {
            if (c.value.hashCode() != hashCode) {
                p = c
                c = c.nextValue
                continue
            }
            if (c.value != value) {
                p = c
                c = c.nextValue
                continue
            }
            return p to c
        }
        return null
    }

    fun put(key: K, value: V, modify: (() -> Unit)?, new: (() -> Unit)?): V? = run {
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
            val e = MutableEntry(key, value)
            e.nextValue = root
            root = e
            size++
            null
        }
    }

    fun removeValue(value: V, modify: (() -> Unit)?): Boolean {
        val en = findValue(value) ?: return false
        en.first?.nextValue = en.second.nextValue
        changeCounter.increment()
        modify?.invoke()
        size--
        return true // en.second.key
    }

    fun remove(key: K, modify: (() -> Unit)?): V? {
        val en = findKey2(key) ?: return null
        en.first?.nextValue = en.second.nextValue
        if (en.first == null) {
            root = en.second.nextValue
        }
        changeCounter.increment()
        modify?.invoke()
        size--
        return en.second.value
    }

    fun clear() {
        changeCounter.increment()
        root = null
        size = 0
    }

    override fun toString(): String {
        val sb = StringBuilder("[")
        var frist = true
        forEach {
            if (!frist) {
                sb.append(", ")
            }
            frist = false
            sb.append("{").append(it.key).append(":").append(it.value).append("]")
            true
        }
        sb.append("]")
        return sb.toString()
    }
}

@Suppress("UNCHECKED_CAST")
class MutableEntry<K, V>(key: K, value: V) : MutableMap.MutableEntry<K, V> {
    private var _key = key
    private var _value = value

    override val key
        get() = _key.let {
            if (it == NullKey) {
                null as K
            } else {
                it
            }
        }
    override val value
        get() = _value
    internal var nextValue: MutableEntry<K, V>? = null

    override fun setValue(newValue: V): V {
        val oldValue = _value
        _value = newValue
        return oldValue
    }

    override fun toString(): String = "{$key:$value}"
}
