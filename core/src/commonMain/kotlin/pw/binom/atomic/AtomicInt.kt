package pw.binom.atomic

expect class AtomicInt(value: Int) {
    fun compareAndSet(expected: Int, new: Int): Boolean
    fun compareAndSwap(expected: Int, new: Int): Int
    fun addAndGet(delta: Int): Int
    fun increment()
    fun decrement()

    var value:Int
}