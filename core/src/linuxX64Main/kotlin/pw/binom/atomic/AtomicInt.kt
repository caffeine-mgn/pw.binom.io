package pw.binom.atomic

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.native.concurrent.AtomicInt as NAtomicInt

actual class AtomicInt actual constructor(value: Int) : ReadWriteProperty<Any?, Int> {
    private val atom = NAtomicInt(value)

    actual fun compareAndSet(expected: Int, new: Int): Boolean =
            atom.compareAndSet(expected, new)

    actual fun compareAndSwap(expected: Int, new: Int): Int =
            atom.compareAndSwap(expected, new)

    actual fun addAndGet(delta: Int): Int =
            atom.addAndGet(delta)

    actual fun increment() {
        atom.increment()
    }

    actual fun decrement() {
        atom.decrement()
    }

    actual var value: Int
        get() = atom.value
        set(value) {
            atom.value = value
        }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        atom.value = value
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int = atom.value
}