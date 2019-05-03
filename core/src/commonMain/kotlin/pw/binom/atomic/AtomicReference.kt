package pw.binom.atomic

expect class AtomicReference<T>(value: T) {
    fun compareAndSet(expected: T, new: T): Boolean
    fun compareAndSwap(expected: T, new: T): T
    var value:T
}