package pw.binom.atomic

import kotlin.properties.ReadWriteProperty

expect class AtomicReference<T>(value: T): ReadWriteProperty<Any?, T> {
    fun compareAndSet(expected: T, new: T): Boolean
    fun compareAndSwap(expected: T, new: T): T
    var value:T
}