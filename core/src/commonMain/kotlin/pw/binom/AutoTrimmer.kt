package pw.binom

/**
 * Wrapper for array list. Makes auto trim to size when [list].size * [trimFactor] < [max]. When it happed
 * call [list].trimToSize() and set [max] to current size. This check happens every [checkSizeCounter] operation.
 *
 * Default value of [trimFactor] is 0.5f
 * Default value of [checkSizeCounter] is 50
 */
open class AutoTrimmer<T>(
    val list: ArrayList<T>,
    var trimFactor: Float = 0.5f,
    var checkSizeCounter: Int = 50
) : RandomAccess, MutableList<T> by list {
    constructor(initialCapacity: Int, trimFactor: Float = 0.5f, checkSizeCounter: Int = 50) : this(
        list = ArrayList(initialCapacity),
        trimFactor = trimFactor,
        checkSizeCounter = checkSizeCounter,
    )

    constructor(elements: Collection<T>, trimFactor: Float = 0.5f, checkSizeCounter: Int = 50) : this(
        list = ArrayList(elements),
        trimFactor = trimFactor,
        checkSizeCounter = checkSizeCounter,
    )

    var max = list.size
        private set
    private var counter = 0

    protected open fun trim() {
        list.trimToSize()
    }

    private fun checkTrim() {
        if (counter <= checkSizeCounter) {
            counter++
            return
        }
        counter = 0
        val size = list.size
        if (size > max) {
            max = size
            return
        }
        if (size * trimFactor < max) {
            trim()
            max = size
        }
    }

    override fun add(element: T): Boolean {
        val e = list.add(element)
        checkTrim()
        return e
    }

    override fun add(index: Int, element: T) {
        list.add(index, element)
        checkTrim()
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val out = list.addAll(index, elements)
        checkTrim()
        return out
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val out = list.addAll(elements)
        checkTrim()
        return out
    }

    override fun remove(element: T): Boolean {
        val out = list.remove(element)
        checkTrim()
        return out
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val out = list.removeAll(elements)
        checkTrim()
        return out
    }

    override fun removeAt(index: Int): T {
        val out = list.removeAt(index)
        checkTrim()
        return out
    }
}

fun <T> ArrayList<T>.autoTrimmed(trimFactor: Float = 0.5f, checkSizeCounter: Int = 50) =
    AutoTrimmer(
        list = this,
        trimFactor = trimFactor,
        checkSizeCounter = checkSizeCounter,
    )
