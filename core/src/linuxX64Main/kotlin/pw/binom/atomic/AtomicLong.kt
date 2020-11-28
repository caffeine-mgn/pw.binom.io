package pw.binom.atomic

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.native.concurrent.AtomicLong as NAtomicLong

actual class AtomicLong actual constructor(value: Long) : ReadWriteProperty<Any, Long> {
    private val atom = NAtomicLong(value)

    actual fun compareAndSet(expected: Long, new: Long): Boolean =
            atom.compareAndSet(expected, new)

    actual fun compareAndSwap(expected: Long, new: Long): Long =
            atom.compareAndSwap(expected, new)

    actual fun addAndGet(delta: Long): Long =
            atom.addAndGet(delta)

    actual fun increment() {
        atom.increment()
    }

    actual fun decrement() {
        atom.decrement()
    }

    actual var value: Long
        get() = atom.value
        set(value) {
            atom.value = value
        }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        atom.value = value
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Long = atom.value
}