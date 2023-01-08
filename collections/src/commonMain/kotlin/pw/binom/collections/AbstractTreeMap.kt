package pw.binom.collections

import kotlin.js.JsName
import kotlin.native.concurrent.SharedImmutable

private const val RED = -1
private const val BLACK = 1

@Suppress("UNCHECKED_CAST")
@SharedImmutable
private val COMPARATOR: Comparator<Any> = Comparator<Any> { a, b ->
    (a as? Comparable<Any>)?.compareTo(b)?.let {
        return@Comparator it
    }
    a.hashCode().compareTo(b.hashCode())
}

@Suppress("UNCHECKED_CAST")
abstract class AbstractTreeMap<K, V>(var comparator: Comparator<K>) : MutableNavigableMap<K, V> {
    private var root: Node<K, V>? = null
    private var modCount = 0
    private var _size = 0

    constructor() : this(COMPARATOR as Comparator<K>)

    protected abstract fun createTreeNode(key: K, value: V, color: Int): Node<K, V>

    interface Node<K, V> : MutableMap.MutableEntry<K, V> {
        var left: Node<K, V>?
        var right: Node<K, V>?
        var parent: Node<K, V>?
        var color: Int
        override var key: K
        override var value: V
    }

    override val size: Int
        get() = _size

    override fun containsKey(key: K): Boolean =
        getNode(key) != null

    override val firstKey: K?
        get() = getFirstNode()?.key

    private fun getFirstNode(): Node<K, V>? {
        // Exploit fact that nil.left == nil.
        var node = root
        while (node?.left != null)
            node = node.left
        return node
    }

    override fun containsValue(value: V): Boolean {
        var node = getFirstNode()
        while (node != null) {
            if (value == node.value) {
                return true
            }
            node = successor(node)
        }
        return false
    }

    /**
     * Return the node following the given one, or nil if there isn't one.
     * Package visible for use by nested classes.
     *
     * @param node the current node, not nil
     * @return the next node in sorted order
     */
    private fun successor(node: Node<K, V>?): Node<K, V>? =
        when {
            node == null -> null
            node.right != null -> {
                var p = node.right
                while (p?.left != null) p = p.left
                p
            }

            else -> {
                var p = node.parent
                var ch = node
                while (p != null && ch === p.right) {
                    ch = p
                    p = p.parent
                }
                p
            }
        }

    private fun <K, V> predecessor(t: Node<K, V>?): Node<K, V>? {
        return if (t == null) null else if (t.left != null) {
            var p: Node<K, V>? = t.left
            while (p?.right != null) p = p?.right
            p
        } else {
            var p: Node<K, V>? = t.parent
            var ch: Node<K, V>? = t
            while (p != null && ch === p.left) {
                ch = p
                p = p.parent
            }
            p
        }
    }

    override fun get(key: K): V? =
        getNode(key)?.value

    override fun isEmpty(): Boolean = root == null

    private inner class EntrySet : AbstractMutableSet<MutableMap.MutableEntry<K, V>>() {
        override val size: Int
            get() = this@AbstractTreeMap.size

        override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
            throw UnsupportedOperationException()
        }

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> = EntryIterator(getFirstNode())

        fun contains(o: Any?): Boolean {
            val entry = (o as? Map.Entry<K, V>) ?: return false
            val value = entry.value
            val p = getNode(entry.key)
            return p != null && p.value == value
        }

        fun remove(o: Any?): Boolean {
            val entry = (o as? Map.Entry<K, V>) ?: return false
            val value = entry.value
            val p = getNode(entry.key)
            if (p != null && p.value == value) {
                removeNode(p)
                return true
            }
            return false
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> = EntrySet()
    override val keys: MutableSet<K>
        get() = TODO("Not yet implemented")
    override val values: MutableCollection<V>
        get() = TODO("Not yet implemented")

    override fun clear() {
        if (size > 0) {
            modCount++
            root = null
            _size = 0
        }
    }

    override fun put(key: K, value: V): V? {
        var current = root
        var parent: Node<K, V>? = null
        var comparison = 0

        // Find new node's parent.
        while (current != null) {
            parent = current
            comparison = compare(key, current.key)
            if (comparison > 0) {
                current = current.right
            } else if (comparison < 0) {
                current = current.left
            } else { // Key already in tree.
                current.value = value
                return value
            }
        }

        // Set up new node.
        val n = createTreeNode(key = key, value = value, color = RED)
        n.parent = parent

        // Insert node in tree.
        modCount++
        _size++
        if (parent == null) {
            // Special case inserting into an empty tree.
            root = n
            return null
        }
        if (comparison > 0) {
            parent.right = n
        } else {
            parent.left = n
        }

        // Rebalance after insert.
        fixAfterInsertion(n)
        return null
    }

    override fun putAll(from: Map<out K, V>) {
        from.entries.forEach {
            put(it.key, it.value)
        }
    }

    override fun remove(key: K): V? {
        val n = getNode(key) ?: return null
        // Note: removeNode can alter the contents of n, so save value now.
        val result = n.value
        removeNode(n)
        return result
    }

    /**
     * Maintain red-black balance after inserting a new node.
     *
     * @param n the newly inserted node
     */
    private fun fixAfterInsertion(n: Node<K, V>) {
        var n: Node<K, V>? = n
        // Only need to rebalance when parent is a RED node, and while at least
        // 2 levels deep into the tree (ie: node has a grandparent). Remember
        // that nil.color == BLACK.
        while (colorOf(n?.parent) == RED && n?.parent?.parent != null) {
            if (n.parent == n.parent!!.parent!!.left) {
                val uncle = n.parent!!.parent!!.right
                // Uncle may be nil, in which case it is BLACK.
                if (colorOf(uncle) == RED) {
                    // Case 1. Uncle is RED: Change colors of parent, uncle,
                    // and grandparent, and move n to grandparent.
                    n.parent!!.color = BLACK
                    uncle!!.color = BLACK
                    uncle.parent!!.color = RED
                    n = uncle.parent
                } else {
                    if (n == n.parent!!.right) {
                        // Case 2. Uncle is BLACK and x is right child.
                        // Move n to parent, and rotate n left.
                        n = n.parent
                        rotateLeft(n!!)
                    }
                    // Case 3. Uncle is BLACK and x is left child.
                    // Recolor parent, grandparent, and rotate grandparent right.
                    n.parent!!.color = BLACK
                    n.parent!!.parent!!.color = RED
                    rotateRight(n.parent!!.parent!!)
                }
            } else {
                // Mirror image of above code.
                var uncle = n.parent!!.parent!!.left
                // Uncle may be nil, in which case it is BLACK.
                if (colorOf(uncle) == RED) {
                    // Case 1. Uncle is RED: Change colors of parent, uncle,
                    // and grandparent, and move n to grandparent.
                    n.parent!!.color = BLACK
                    uncle!!.color = BLACK
                    uncle.parent!!.color = RED
                    n = uncle.parent
                } else {
                    if (n == n.parent!!.left) {
                        // Case 2. Uncle is BLACK and x is left child.
                        // Move n to parent, and rotate n right.
                        n = n.parent
                        rotateRight(n!!)
                    }
                    // Case 3. Uncle is BLACK and x is right child.
                    // Recolor parent, grandparent, and rotate grandparent left.
                    n.parent!!.color = BLACK
                    n.parent!!.parent!!.color = RED
                    rotateLeft(n.parent!!.parent!!)
                }
            }
        }
        root!!.color = BLACK
    }

    fun getNode(key: K): Node<K, V>? {
        var current = root
        while (current != null) {
            val comparison = compare(key, current.key)
            current = when {
                comparison > 0 -> current.right
                comparison < 0 -> current.left
                else -> return current
            }
        }
        return current
    }

    /**
     * Remove node from tree. This will increment modCount and decrement size.
     * Node must exist in the tree. Package visible for use by nested classes.
     *
     * @param node the node to remove
     */
    private fun removeNode(node: Node<K, V>) {
        var splice: Node<K, V>?
        val child: Node<K, V>?

        modCount++
        _size--

        // Find splice, the node at the position to actually remove from the tree.
        when {
            node.left == null -> {
                // Node to be deleted has 0 or 1 children.
                splice = node
                child = node.right
            }

            node.right == null -> {
                // Node to be deleted has 1 child.
                splice = node
                child = node.left
            }

            else -> {
                // Node has 2 children. Splice is node's predecessor, and we swap
                // its contents into node.
                splice = node.left
                while (splice?.right != null) {
                    splice = splice.right
                }
                splice!!
                child = splice.left
                node.key = splice.key
                node.value = splice.value
            }
        }

        // Unlink splice from the tree.
        val parent = splice.parent
        if (child != null) {
            child.parent = parent
        }
        if (parent == null) {
            // Special case for 0 or 1 node remaining.
            root = child
            return
        }
        if (splice == parent.left) {
            parent.left = child
        } else {
            parent.right = child
        }

        if (colorOf(splice) == BLACK) {
            fixAfterDeletion(child, parent)
        }
    }

    private fun colorOf(node: Node<K, V>?) = node?.color ?: BLACK

    /**
     * Maintain red-black balance after deleting a node.
     *
     * @param node the child of the node just deleted, possibly nil
     * @param parent the parent of the node just deleted, never nil
     */
    private fun fixAfterDeletion(node: Node<K, V>?, parent: Node<K, V>) {
        var node: Node<K, V>? = node
        var parent: Node<K, V>? = parent
        // if (parent == nil)
        //   throw new InternalError();
        // If a black node has been removed, we need to rebalance to avoid
        // violating the "same number of black nodes on any path" rule. If
        // node is red, we can simply recolor it black and all is well.
        while (node != root && colorOf(node) == BLACK) {
            if (node == parent!!.left) {
                // Rebalance left side.
                var sibling = parent.right
                // if (sibling == nil)
                //   throw new InternalError();
                if (colorOf(sibling) == RED) {
                    // Case 1: Sibling is red.
                    // Recolor sibling and parent, and rotate parent left.
                    sibling?.color = BLACK
                    parent.color = RED
                    rotateLeft(parent)
                    sibling = parent.right
                }

                if (colorOf(sibling!!.left) == BLACK && colorOf(sibling.right) == BLACK) {
                    // Case 2: Sibling has no red children.
                    // Recolor sibling, and move to parent.
                    sibling.color = RED
                    node = parent
                    parent = parent.parent
                } else {
                    if (sibling.right!!.color == BLACK) {
                        // Case 3: Sibling has red left child.
                        // Recolor sibling and left child, rotate sibling right.
                        sibling.left!!.color = BLACK
                        sibling.color = RED
                        rotateRight(sibling)
                        sibling = parent.right
                    }
                    // Case 4: Sibling has red right child. Recolor sibling,
                    // right child, and parent, and rotate parent left.
                    sibling!!.color = parent.color
                    parent.color = BLACK
                    sibling.right!!.color = BLACK
                    rotateLeft(parent)
                    node = root // Finished.
                }
            } else {
                // Symmetric "mirror" of left-side case.
                var sibling = parent.left
                // if (sibling == nil)
                //   throw new InternalError()
                if (colorOf(sibling) == RED) {
                    // Case 1: Sibling is red.
                    // Recolor sibling and parent, and rotate parent right.
                    sibling?.color = BLACK
                    parent.color = RED
                    rotateRight(parent)
                    sibling = parent.left
                }

                if (colorOf(sibling!!.right) == BLACK && colorOf(sibling.left) == BLACK) {
                    // Case 2: Sibling has no red children.
                    // Recolor sibling, and move to parent.
                    sibling.color = RED
                    node = parent
                    parent = parent.parent
                } else {
                    if (colorOf(sibling.left) == BLACK) {
                        // Case 3: Sibling has red right child.
                        // Recolor sibling and right child, rotate sibling left.
                        sibling.right!!.color = BLACK
                        sibling.color = RED
                        rotateLeft(sibling)
                        sibling = parent.left
                    }
                    // Case 4: Sibling has red left child. Recolor sibling,
                    // left child, and parent, and rotate parent right.
                    sibling?.color = parent.color
                    parent.color = BLACK
                    sibling!!.left!!.color = BLACK
                    rotateRight(parent)
                    node = root // Finished.
                }
            }
        }
        node!!.color = BLACK
    }

    /**
     * Rotate node n to the right.
     *
     * @param node the node to rotate
     */
    private fun rotateRight(node: Node<K, V>) {
        val child = node.left
        // if (node == nil || child == nil)
        //   throw new InternalError();

        // Establish node.left link.
        node.left = child!!.right
        if (child.right != null) {
            child.right!!.parent = node
        }

        // Establish child->parent link.
        child.parent = node.parent
        if (node.parent != null) {
            if (node == node.parent!!.right) {
                node.parent!!.right = child
            } else {
                node.parent!!.left = child
            }
        } else {
            root = child
        }

        // Link n and child.
        child.right = node
        node.parent = child
    }

    /**
     * Rotate node n to the left.
     *
     * @param node the node to rotate
     */
    private fun rotateLeft(node: Node<K, V>) {
        val node = node
        val child = node.right
        // if (node == nil || child == nil)
        //   throw new InternalError();

        // Establish node.right link.
        node.right = child!!.left
        if (child.left != null) {
            child.left!!.parent = node
        }

        // Establish child->parent link.
        child.parent = node.parent
        if (node.parent != null) {
            if (node == node.parent!!.left) {
                node.parent!!.left = child
            } else {
                node.parent!!.right = child
            }
        } else {
            root = child
        }

        // Link n and child.
        child.left = node
        node.parent = child
    }

    private fun compare(a: K, b: K) =
        comparator.compare(a, b)

    fun getLastNode(): Node<K, V>? {
        var p = root
        if (p != null) {
            while (p?.right != null) {
                p = p.right
            }
        }
        return p
    }

    override val lastKey: K?
        get() = getLastNode()?.key

    override val firstEntry: Map.Entry<K, V>?
        get() = getFirstNode()

    override val lastEntry
        get() = getLastNode()

    override fun pollFirstEntry(): Map.Entry<K, V>? {
        val p = getFirstNode() ?: return null
        val result = MapNode(p.key, p.value)
        removeNode(p)
        return result
    }

    override fun pollLastEntry(): Map.Entry<K, V>? {
        val p = lastEntry ?: return null
        val result = MapNode(p.key, p.value)
        removeNode(p)
        return result
    }

    fun getLowerEntry(key: K): Node<K, V>? {
        var p = root
        while (p != null) {
            var cmp = compare(key, p.key)
            if (cmp > 0) {
                if (p.right != null) {
                    p = p.right
                } else {
                    return p
                }
            } else {
                if (p.left != null) {
                    p = p.left
                } else {
                    var parent = p.parent
                    var ch = p
                    while (parent != null && ch == parent.left) {
                        ch = parent
                        parent = parent.parent
                    }
                    return parent
                }
            }
        }
        return null
    }

    fun getHigherEntry(key: K): Node<K, V>? {
        var p = this.root
        while (p != null) {
            val cmp = this.compare(key, p.key)
            if (cmp < 0) {
                if (p.left == null) {
                    return p
                }
                p = p.left
            } else {
                if (p.right == null) {
                    var parent = p.parent
                    var ch = p
                    while (parent != null && ch == parent.right) {
                        ch = parent
                        parent = parent.parent
                    }
                    return parent
                }
                p = p.right
            }
        }
        return null
    }

    fun getFloorEntry(key: K): Node<K, V>? {
        var p = this.root

        while (p != null) {
            val cmp = this.compare(key, p.key)
            if (cmp > 0) {
                if (p.right == null) {
                    return p
                }
                p = p.right
            } else {
                if (cmp >= 0) {
                    return p
                }
                if (p.left == null) {
                    var parent = p.parent
                    var ch = p
                    while (parent != null && ch == parent.left) {
                        ch = parent
                        parent = parent.parent
                    }
                    return parent
                }
                p = p.left
            }
        }
        return null
    }

    fun getCeilingEntry(key: K): Node<K, V>? {
        var p = this.root

        while (p != null) {
            val cmp = this.compare(key, p.key)
            if (cmp < 0) {
                if (p.left == null) {
                    return p
                }
                p = p.left
            } else {
                if (cmp <= 0) {
                    return p
                }
                if (p.right == null) {
                    var parent = p.parent
                    var ch = p
                    while (parent != null && ch == parent.right) {
                        ch = parent
                        parent = parent.parent
                    }
                    return parent
                }
                p = p.right
            }
        }
        return null
    }

    override fun lowerEntry(key: K): Node<K, V>? =
        getLowerEntry(key)

    override fun floorEntry(key: K): Map.Entry<K, V>? =
        getFloorEntry(key)

    override fun ceilingEntry(key: K): Map.Entry<K, V>? =
        getCeilingEntry(key)

    override fun higherEntry(key: K): Map.Entry<K, V>? =
        getHigherEntry(key)

    abstract inner class PrivateEntryIterator<T>(first: Node<K, V>?) : MutableIterator<T> {
        @JsName("next2")
        var next: Node<K, V>? = null
        var lastReturned: Node<K, V>? = null
        var expectedModCount: Int

        init {
            expectedModCount = modCount
            lastReturned = null
            next = first
        }

        override fun hasNext(): Boolean {
            return next != null
        }

        fun nextEntry(): Node<K, V> {
            val e = next ?: throw NoSuchElementException()
            if (modCount != expectedModCount) throw ConcurrentModificationException()
            next = successor(e)
            lastReturned = e
            return e
        }

        fun prevEntry(): Node<K, V> {
            val e = next ?: throw NoSuchElementException()
            if (modCount != expectedModCount) throw ConcurrentModificationException()
            next = predecessor<K, V>(e)
            lastReturned = e
            return e
        }

        override fun remove() {
            val lastReturned = lastReturned ?: throw IllegalStateException()
            if (modCount != expectedModCount) throw ConcurrentModificationException()
            // deleted entries are replaced by their successors
            if (lastReturned.left != null && lastReturned.right != null) next = lastReturned
            removeNode(lastReturned)
            expectedModCount = modCount
            this.lastReturned = null
        }
    }

    private inner class EntryIterator(first: Node<K, V>?) : PrivateEntryIterator<MutableMap.MutableEntry<K, V>>(first) {
        override operator fun next(): MutableMap.MutableEntry<K, V> {
            return nextEntry()
        }
    }

    private inner class ValueIterator(first: Node<K, V>?) : PrivateEntryIterator<V>(first) {
        override operator fun next(): V {
            return nextEntry().value
        }
    }

    private inner class KeyIterator(first: Node<K, V>?) : PrivateEntryIterator<K?>(first) {
        override operator fun next(): K {
            return nextEntry().key
        }
    }
}

/*
internal class KeySet<E>(map: MutableNavigableMap<E, *>) : AbstractMutableSet<E>(), MutableNavigableSet<E> {
    private val m = map

    override operator fun iterator(): MutableIterator<E> {
        return if (m is TreeMap) (m as TreeMap<E, *>).keyIterator() else (m as NavigableSubMap<E, *>).keyIterator()
    }

    override fun descendingIterator(): Iterator<E> {
        return if (m is TreeMap) (m as TreeMap<E, *>).descendingKeyIterator() else (m as NavigableSubMap<E, *>).descendingKeyIterator()
    }

    override fun size(): Int {
        return m.size
    }

    @JsName("isEmpty2")
    val isEmpty: Boolean
        get() = m.isEmpty()

    override operator fun contains(o: Any): Boolean {
        return m.containsKey(o)
    }

    override fun clear() {
        m.clear()
    }

    override fun lower(e: E): E {
        return m.lowerKey(e)
    }

    override fun floor(e: E): E {
        return m.floorKey(e)
    }

    override fun ceiling(e: E): E {
        return m.ceilingKey(e)
    }

    override fun higher(e: E): E {
        return m.higherKey(e)
    }

    override fun first(): E {
        return m.firstKey()
    }

    override fun last(): E {
        return m.lastKey()
    }

    override fun comparator(): java.util.Comparator<in E> {
        return m.comparator()
    }

    override fun pollFirst(): E {
        val e: Map.Entry<E, *> = m.pollFirstEntry()
        return if (e == null) null else e.key
    }

    override fun pollLast(): E {
        val e: Map.Entry<E, *> = m.pollLastEntry()
        return if (e == null) null else e.key
    }

    override fun remove(o: Any): Boolean {
        val oldSize = size
        m.remove(o)
        return size != oldSize
    }

    override fun subSet(
        fromElement: E,
        fromInclusive: Boolean,
        toElement: E,
        toInclusive: Boolean
    ): NavigableSet<E> {
        return KeySet<E>(
            m.subMap(
                fromElement,
                fromInclusive,
                toElement,
                toInclusive
            )
        )
    }

    override fun headSet(toElement: E, inclusive: Boolean): NavigableSet<E> {
        return KeySet<E>(m.headMap(toElement, inclusive))
    }

    override fun tailSet(fromElement: E, inclusive: Boolean): NavigableSet<E> {
        return KeySet<E>(m.tailMap(fromElement, inclusive))
    }

    override fun subSet(fromElement: E, toElement: E): SortedSet<E> {
        return subSet(fromElement, true, toElement, false)
    }

    override fun headSet(toElement: E): SortedSet<E> {
        return headSet(toElement, false)
    }

    override fun tailSet(fromElement: E): SortedSet<E> {
        return tailSet(fromElement, true)
    }

    override fun descendingSet(): java.util.NavigableSet<E> {
        return KeySet<E>(m.descendingMap())
    }

    override fun spliterator(): java.util.Spliterator<E> {
        return java.util.TreeMap.keySpliteratorFor<E>(m)
    }
}
*/
class MapNode<K, V>(override val key: K, override val value: V) : Map.Entry<K, V>
