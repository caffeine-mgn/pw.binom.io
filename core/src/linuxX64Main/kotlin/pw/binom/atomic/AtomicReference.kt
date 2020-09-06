package pw.binom.atomic

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.native.concurrent.AtomicReference as NAtomicReference

actual class AtomicReference<T> actual constructor(value: T) : ReadWriteProperty<Any, T> {
    private val atom = NAtomicReference(value)

    actual fun compareAndSet(expected: T, new: T): Boolean =
            atom.compareAndSet(expected, new)

    actual fun compareAndSwap(expected: T, new: T): T =
            atom.compareAndSwap(expected, new)

    actual var value: T
        get() = atom.value
        set(value) {
            atom.value = value
        }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        atom.value = value
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T = atom.value
}