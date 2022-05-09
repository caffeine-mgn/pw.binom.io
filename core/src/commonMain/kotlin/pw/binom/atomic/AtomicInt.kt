package pw.binom.atomic

import kotlin.properties.ReadWriteProperty

expect class AtomicInt(value: Int) : ReadWriteProperty<Any?, Int> {
    fun compareAndSet(expected: Int, new: Int): Boolean
    fun compareAndSwap(expected: Int, new: Int): Int
    fun addAndGet(delta: Int): Int
    fun increment()
    fun decrement()
    operator fun inc(): AtomicInt
    operator fun dec(): AtomicInt

    var value: Int
}
