package pw.binom.atomic

import kotlin.properties.ReadWriteProperty

expect class AtomicLong(value: Long) : ReadWriteProperty<Any, Long> {
    fun compareAndSet(expected: Long, new: Long): Boolean
    fun compareAndSwap(expected: Long, new: Long): Long
    fun addAndGet(delta: Long): Long
    fun increment()
    fun decrement()

    var value: Long
}