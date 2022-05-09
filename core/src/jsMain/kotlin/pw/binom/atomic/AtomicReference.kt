package pw.binom.atomic

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

actual class AtomicReference<T> actual constructor(actual var value: T) : ReadWriteProperty<Any?, T> {
    actual fun compareAndSet(expected: T, new: T): Boolean {
        if (value === expected) {
            value = new
            return true
        }
        return false
    }

    actual fun compareAndSwap(expected: T, new: T): T {
        if (value === expected) {
            val old = value
            value = new
            return old
        }
        return new
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}
