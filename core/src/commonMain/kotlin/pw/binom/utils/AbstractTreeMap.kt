package pw.binom.utils

import pw.binom.doFreeze
import kotlin.native.concurrent.SharedImmutable

private var RED = -1
private var BLACK = 1

@SharedImmutable
private val COMPARATOR: Comparator<Any> = Comparator<Any> { a, b ->
    a as Comparable<Any>
    a.compareTo(b)
}.doFreeze()

abstract class AbstractTreeMap<K, V>() : MutableNavigableMap<K, V> {
    private var root: Node<K, V>? = null
    private var modCount = 0
    private var _size = 0
    var comparator = COMPARATOR as Comparator<K>

    constructor(comparator: Comparator<K>) : this() {
        this.comparator = comparator
    }

    protected abstract fun createTreeNode(key: K, value: V, color: Int): Node<K, V>

    interface Node<K, V> : Map.Entry<K, V> {
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
        var node = root;
        while (node?.left != null)
            node = node.left;
        return node
    }

    override fun containsValue(value: V): Boolean {
        var node = getFirstNode()
        while (node != null) {
            if (value == node.value)
                return true;
            node = successor(node);
        }
        return false;
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

    override fun get(key: K): V? =
        getNode(key)?.value

    override fun isEmpty(): Boolean = root == null

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = TODO("Not yet implemented")
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
        var current = root;
        var parent: Node<K, V>? = null
        var comparison = 0

        // Find new node's parent.
        while (current != null) {
            parent = current;
            comparison = compare(key, current.key);
            if (comparison > 0)
                current = current.right;
            else if (comparison < 0)
                current = current.left;
            else { // Key already in tree.
                current.value = value
                return value
            }
        }

        // Set up new node.
        val n = createTreeNode(key = key, value = value, color = RED);
        n.parent = parent

        // Insert node in tree.
        modCount++;
        _size++;
        if (parent == null) {
            // Special case inserting into an empty tree.
            root = n;
            return null;
        }
        if (comparison > 0)
            parent.right = n;
        else
            parent.left = n;

        // Rebalance after insert.
        fixAfterInsertion(n);
        return null;
    }

    override fun putAll(from: Map<out K, V>) {
        from.entries.forEach {
            put(it.key, it.value)
        }
    }

    override fun remove(key: K): V? {
        val n = getNode(key) ?: return null;
        // Note: removeNode can alter the contents of n, so save value now.
        val result = n.value;
        removeNode(n);
        return result;
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
                var uncle = n.parent!!.parent!!.right;
                // Uncle may be nil, in which case it is BLACK.
                if (colorOf(uncle) == RED) {
                    // Case 1. Uncle is RED: Change colors of parent, uncle,
                    // and grandparent, and move n to grandparent.
                    n.parent!!.color = BLACK;
                    uncle!!.color = BLACK;
                    uncle.parent!!.color = RED;
                    n = uncle.parent;
                } else {
                    if (n == n!!.parent!!.right) {
                        // Case 2. Uncle is BLACK and x is right child.
                        // Move n to parent, and rotate n left.
                        n = n.parent;
                        rotateLeft(n!!);
                    }
                    // Case 3. Uncle is BLACK and x is left child.
                    // Recolor parent, grandparent, and rotate grandparent right.
                    n.parent!!.color = BLACK;
                    n.parent!!.parent!!.color = RED;
                    rotateRight(n.parent!!.parent!!);
                }
            } else {
                // Mirror image of above code.
                var uncle = n!!.parent!!.parent!!.left;
                // Uncle may be nil, in which case it is BLACK.
                if (colorOf(uncle) == RED) {
                    // Case 1. Uncle is RED: Change colors of parent, uncle,
                    // and grandparent, and move n to grandparent.
                    n.parent!!.color = BLACK;
                    uncle!!.color = BLACK;
                    uncle!!.parent!!.color = RED;
                    n = uncle!!.parent
                } else {
                    if (n == n!!.parent!!.left) {
                        // Case 2. Uncle is BLACK and x is left child.
                        // Move n to parent, and rotate n right.
                        n = n.parent;
                        rotateRight(n!!);
                    }
                    // Case 3. Uncle is BLACK and x is right child.
                    // Recolor parent, grandparent, and rotate grandparent left.
                    n!!.parent!!.color = BLACK;
                    n!!.parent!!.parent!!.color = RED;
                    rotateLeft(n!!.parent!!.parent!!);
                }
            }
        }
        root!!.color = BLACK;
    }

    fun getNode(key: K): Node<K, V>? {
        var current = root;
        while (current != null) {
            val comparison = compare(key, current.key);
            current = when {
                comparison > 0 -> current.right
                comparison < 0 -> current.left
                else -> return current
            }
        }
        return current;
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
                    sibling?.color = BLACK;
                    parent.color = RED;
                    rotateLeft(parent);
                    sibling = parent.right;
                }

                if (colorOf(sibling!!.left) == BLACK && colorOf(sibling.right) == BLACK) {
                    // Case 2: Sibling has no red children.
                    // Recolor sibling, and move to parent.
                    sibling!!.color = RED;
                    node = parent;
                    parent = parent.parent;
                } else {
                    if (sibling.right!!.color == BLACK) {
                        // Case 3: Sibling has red left child.
                        // Recolor sibling and left child, rotate sibling right.
                        sibling.left!!.color = BLACK;
                        sibling.color = RED;
                        rotateRight(sibling);
                        sibling = parent!!.right;
                    }
                    // Case 4: Sibling has red right child. Recolor sibling,
                    // right child, and parent, and rotate parent left.
                    sibling!!.color = parent!!.color;
                    parent.color = BLACK;
                    sibling.right!!.color = BLACK;
                    rotateLeft(parent);
                    node = root // Finished.
                }
            } else {
                // Symmetric "mirror" of left-side case.
                var sibling = parent.left;
                // if (sibling == nil)
                //   throw new InternalError();
                if (colorOf(sibling) == RED) {
                    // Case 1: Sibling is red.
                    // Recolor sibling and parent, and rotate parent right.
                    sibling?.color = BLACK;
                    parent.color = RED;
                    rotateRight(parent);
                    sibling = parent.left;
                }

                if (colorOf(sibling!!.right) == BLACK && colorOf(sibling.left) == BLACK) {
                    // Case 2: Sibling has no red children.
                    // Recolor sibling, and move to parent.
                    sibling.color = RED;
                    node = parent;
                    parent = parent.parent;
                } else {
                    if (colorOf(sibling.left) == BLACK) {
                        // Case 3: Sibling has red right child.
                        // Recolor sibling and right child, rotate sibling left.
                        sibling.right!!.color = BLACK;
                        sibling.color = RED;
                        rotateLeft(sibling);
                        sibling = parent!!.left;
                    }
                    // Case 4: Sibling has red left child. Recolor sibling,
                    // left child, and parent, and rotate parent right.
                    sibling?.color = parent.color;
                    parent.color = BLACK;
                    sibling!!.left!!.color = BLACK;
                    rotateRight(parent);
                    node = root // Finished.
                }
            }
        }
        node!!.color = BLACK;
    }

    /**
     * Rotate node n to the right.
     *
     * @param node the node to rotate
     */
    private fun rotateRight(node: Node<K, V>) {
        var child = node.left
        // if (node == nil || child == nil)
        //   throw new InternalError();

        // Establish node.left link.
        node.left = child!!.right;
        if (child.right != null)
            child.right!!.parent = node;

        // Establish child->parent link.
        child.parent = node.parent;
        if (node.parent != null) {
            if (node == node.parent!!.right)
                node.parent!!.right = child;
            else
                node.parent!!.left = child;
        } else
            root = child;

        // Link n and child.
        child.right = node;
        node.parent = child;
    }

    /**
     * Rotate node n to the left.
     *
     * @param node the node to rotate
     */
    private fun rotateLeft(node: Node<K, V>) {
        val node = node
        val child = node.right;
        // if (node == nil || child == nil)
        //   throw new InternalError();

        // Establish node.right link.
        node.right = child!!.left;
        if (child.left != null)
            child.left!!.parent = node;

        // Establish child->parent link.
        child.parent = node.parent;
        if (node.parent != null) {
            if (node == node.parent!!.left)
                node.parent!!.left = child;
            else
                node.parent!!.right = child;
        } else
            root = child;

        // Link n and child.
        child.left = node;
        node.parent = child;
    }

    private fun compare(a: K, b: K) =
        comparator.compare(a, b)

    fun getLastNode(): Node<K, V>? {
        var p = root
        if (p != null)
            while (p?.right != null) {
                p = p.right
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
        var p = root;
        while (p != null) {
            var cmp = compare(key, p.key);
            if (cmp > 0) {
                if (p.right != null)
                    p = p.right;
                else
                    return p;
            } else {
                if (p.left != null) {
                    p = p.left;
                } else {
                    var parent = p.parent;
                    var ch = p;
                    while (parent != null && ch == parent.left) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            }
        }
        return null
    }

    override fun lowerEntry(key: K) =
        getLowerEntry(key)

    override fun floorEntry(key: K): Map.Entry<K, V> {
        TODO("Not yet implemented")
    }

    override fun ceilingEntry(key: K): Map.Entry<K, V> {
        TODO("Not yet implemented")
    }

    override fun higherEntry(key: K): Map.Entry<K, V> {
        TODO("Not yet implemented")
    }
}

class MapNode<K, V>(override val key: K, override val value: V) : Map.Entry<K, V>