package pw.binom.collections

class PriorityQueue<T> private constructor(val map: TreeMap<T, Boolean?>) : Queue<T> {
    override val isEmpty: Boolean
        get() = map.isEmpty()
    override val size: Int
        get() = map.size

    override fun pop(): T {
        val e = map.pollFirstEntry() ?: throw NoSuchElementException()
        return e.key
    }

    override fun peek(): T {
        val e = map.firstEntry ?: throw NoSuchElementException()
        return e.key
    }

//    override fun peek(dest: PopResult<T>): Boolean {
//        dest.set(map.firstEntry?.key ?: return false)
//        return true
//    }

    override fun pop(dist: PopResult<T>) {
        val e = map.pollFirstEntry()
        if (e == null) {
            dist.clear()
            return
        }
        dist.set(e.key)
    }
}
