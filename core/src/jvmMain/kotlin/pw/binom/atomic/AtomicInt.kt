package pw.binom.atomic

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import java.util.concurrent.atomic.AtomicInteger as JAtomicInt

actual class AtomicInt actual constructor(value: Int) : ReadWriteProperty<Any?, Int> {
    private val atom = JAtomicInt(value)

    actual fun compareAndSet(expected: Int, new: Int): Boolean =
            atom.compareAndSet(expected, new)

    actual fun compareAndSwap(expected: Int, new: Int): Int =
            atom.compareAndExchange(expected, new)

    actual fun addAndGet(delta: Int): Int =
            atom.addAndGet(delta)

    actual fun increment() {
        atom.incrementAndGet()
    }

    actual fun decrement() {
        atom.decrementAndGet()
    }

    actual var value: Int
        get() = atom.get()
        set(value) {
            atom.set(value)
        }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        atom.set(value)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int = atom.get()
    actual operator fun inc(): AtomicInt {
        increment()
        return this
    }

    actual operator fun dec(): AtomicInt {
        decrement()
        return this
    }
}