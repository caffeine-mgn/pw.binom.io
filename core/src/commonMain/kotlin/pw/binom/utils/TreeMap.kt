package pw.binom.utils

class TreeMap<K, V> : AbstractTreeMap<K, V> {
    constructor() : super()
    constructor(comparator: Comparator<K>) : super(comparator)

    private class NodeImpl<K, V>(override var color: Int, override var key: K, override var value: V) : Node<K, V> {
        override var left: Node<K, V>? = null
        override var right: Node<K, V>? = null
        override var parent: Node<K, V>? = null
    }

    override fun createTreeNode(key: K, value: V, color: Int): Node<K, V> =
        NodeImpl(
            key = key,
            value = value,
            color = color
        )
}