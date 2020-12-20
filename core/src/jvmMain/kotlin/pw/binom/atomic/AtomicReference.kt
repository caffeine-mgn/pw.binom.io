package pw.binom.atomic

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import java.util.concurrent.atomic.AtomicReference as JAtomicReference

actual class AtomicReference<T> actual constructor(value: T): ReadWriteProperty<Any?, T> {
    private val atom = JAtomicReference(value)

    actual fun compareAndSet(expected: T, new: T): Boolean =
            atom.compareAndSet(expected, new)

    actual fun compareAndSwap(expected: T, new: T): T =
            atom.compareAndExchange(expected, new)

    actual var value: T
        get() = atom.get()
        set(value) {
            atom.set(value)
        }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        atom.set(value)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = atom.get()
}