package pw.binom.atomic

import kotlin.native.concurrent.AtomicInt as NAtomicInt

actual class AtomicInt actual constructor(value: Int) {
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
}