package pw.binom.atomic

import java.util.concurrent.atomic.AtomicInteger as JAtomicInt

actual class AtomicInt actual constructor(value: Int) {
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
}