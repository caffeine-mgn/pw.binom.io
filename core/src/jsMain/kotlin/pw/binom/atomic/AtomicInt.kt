package pw.binom.atomic

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

actual class AtomicInt actual constructor(actual var value: Int) : ReadWriteProperty<Any?, Int> {
    actual fun compareAndSet(expected: Int, new: Int): Boolean {
        if (value == expected) {
            value = new
            return true
        }
        return false
    }

    actual fun compareAndSwap(expected: Int, new: Int): Int {
        if (value == expected) {
            val old = value
            value = new
            return old
        }
        return new
    }

    actual fun addAndGet(delta: Int): Int {
        value + delta
        return value
    }

    actual fun increment() {
        value++
    }

    actual fun decrement() {
        value--
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        this.value = value
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int = value

    actual operator fun inc(): AtomicInt {
        increment()
        return this
    }

    actual operator fun dec(): AtomicInt {
        decrement()
        return this
    }
}
